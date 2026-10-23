package com.example.accounting.service.impl;

import com.example.accounting.dto.AccountSideTotal;
import com.example.accounting.dto.BalanceSheetResponse;
import com.example.accounting.dto.CashFlowStatementResponse;
import com.example.accounting.dto.FinancialStatementLine;
import com.example.accounting.dto.IncomeStatementResponse;
import com.example.accounting.entity.AccountType;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalEntry;
import com.example.accounting.entity.JournalEntryLine;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalEntryLineRepository;
import com.example.accounting.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

// Covers the three financial statements. The cases that matter here are the ones where
// getting the accounting wrong produces a plausible-looking but incorrect statement:
// normal-balance normalization, the unclosed-P&L equity line that makes the balance
// sheet balance at all, and cash flow's attribution rules (internal transfers excluded,
// multi-line splits reconciling exactly to the cash accounts).
@ExtendWith(MockitoExtension.class)
class FinancialStatementServiceImplTest {

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @Mock
    private GlAccountRepository glAccountRepository;

    @InjectMocks
    private FinancialStatementServiceImpl service;

    private GlAccount cash;
    private GlAccount loansReceivable;
    private GlAccount customerDeposits;
    private GlAccount ownersCapital;
    private GlAccount interestIncome;
    private GlAccount operatingExpenses;

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 1, 31);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "cashAccountNumbers", List.of("1010"));

        cash = account(1L, "1010", "Cash and Cash Equivalents", AccountType.ASSET, EntrySide.DEBIT);
        loansReceivable = account(2L, "1100", "Loans Receivable", AccountType.ASSET, EntrySide.DEBIT);
        customerDeposits = account(3L, "2100", "Customer Deposits", AccountType.LIABILITY, EntrySide.CREDIT);
        ownersCapital = account(4L, "3010", "Owner's Capital", AccountType.EQUITY, EntrySide.CREDIT);
        interestIncome = account(5L, "4010", "Interest Income", AccountType.INCOME, EntrySide.CREDIT);
        operatingExpenses = account(6L, "5020", "Operating Expenses", AccountType.EXPENSE, EntrySide.DEBIT);
    }

    // ── income statement ─────────────────────────────────────────────────────

    @Test
    void incomeStatementNormalizesEachAccountToItsNormalBalance() {
        // Income is credit-normal, so a debit against it (a reversal) reduces income;
        // expense is debit-normal, so a credit against it reduces expense.
        when(journalEntryLineRepository.aggregateByAccountAndSideForDateRange(FROM, TO)).thenReturn(List.of(
                total(interestIncome, EntrySide.CREDIT, "1000.00"),
                total(interestIncome, EntrySide.DEBIT, "150.00"),
                total(operatingExpenses, EntrySide.DEBIT, "400.00"),
                total(operatingExpenses, EntrySide.CREDIT, "25.00")));
        stubAccounts(interestIncome, operatingExpenses);

        IncomeStatementResponse result = service.getIncomeStatement(FROM, TO);

        assertThat(result.getTotalIncome()).isEqualByComparingTo("850.00");
        assertThat(result.getTotalExpenses()).isEqualByComparingTo("375.00");
        assertThat(result.getNetIncome()).isEqualByComparingTo("475.00");
        assertThat(result.getIncome()).extracting(FinancialStatementLine::getAccountNo).containsExactly("4010");
        assertThat(result.getExpenses()).extracting(FinancialStatementLine::getAccountNo).containsExactly("5020");
    }

    @Test
    void incomeStatementExcludesBalanceSheetAccounts() {
        when(journalEntryLineRepository.aggregateByAccountAndSideForDateRange(FROM, TO)).thenReturn(List.of(
                total(interestIncome, EntrySide.CREDIT, "500.00"),
                total(loansReceivable, EntrySide.DEBIT, "9000.00")));
        stubAccounts(interestIncome, loansReceivable);

        IncomeStatementResponse result = service.getIncomeStatement(FROM, TO);

        assertThat(result.getIncome()).hasSize(1);
        assertThat(result.getExpenses()).isEmpty();
        assertThat(result.getNetIncome()).isEqualByComparingTo("500.00");
    }

    // ── balance sheet ────────────────────────────────────────────────────────

    @Test
    void balanceSheetCarriesUnclosedProfitAsEquitySoItBalances() {
        // Assets 10,000 = Liabilities 3,000 + Equity 6,000 + retained P&L 1,000.
        // Without the currentEarnings line this sheet would look 1,000 out.
        when(journalEntryLineRepository.aggregateByAccountAndSideForDateRange(isNull(), eq(TO))).thenReturn(List.of(
                total(cash, EntrySide.DEBIT, "10000.00"),
                total(customerDeposits, EntrySide.CREDIT, "3000.00"),
                total(ownersCapital, EntrySide.CREDIT, "6000.00"),
                total(interestIncome, EntrySide.CREDIT, "1200.00"),
                total(operatingExpenses, EntrySide.DEBIT, "200.00")));
        stubAccounts(cash, customerDeposits, ownersCapital, interestIncome, operatingExpenses);

        BalanceSheetResponse result = service.getBalanceSheet(TO);

        assertThat(result.getTotalAssets()).isEqualByComparingTo("10000.00");
        assertThat(result.getTotalLiabilities()).isEqualByComparingTo("3000.00");
        assertThat(result.getTotalEquity()).isEqualByComparingTo("6000.00");
        assertThat(result.getCurrentEarnings()).isEqualByComparingTo("1000.00");
        assertThat(result.getTotalLiabilitiesAndEquity()).isEqualByComparingTo("10000.00");
        assertThat(result.isBalanced()).isTrue();
    }

    @Test
    void balanceSheetReportsUnbalancedWhenPostingsDoNotAddUp() {
        when(journalEntryLineRepository.aggregateByAccountAndSideForDateRange(isNull(), eq(TO))).thenReturn(List.of(
                total(cash, EntrySide.DEBIT, "10000.00"),
                total(ownersCapital, EntrySide.CREDIT, "6000.00")));
        stubAccounts(cash, ownersCapital);

        BalanceSheetResponse result = service.getBalanceSheet(TO);

        assertThat(result.isBalanced()).isFalse();
    }

    // ── cash flow ────────────────────────────────────────────────────────────

    @Test
    void cashFlowAttributesEachMovementToItsCounterpartAndReconciles() {
        stubOpeningCash("2000.00", "500.00");
        // Disbursement: cash out, funding a loan receivable. Repayment: cash in from the
        // same receivable. Both are the two-line shape the journal templates generate.
        JournalEntry disbursement = entry(
                line(cash, EntrySide.CREDIT, "1000.00"),
                line(loansReceivable, EntrySide.DEBIT, "1000.00"));
        JournalEntry repayment = entry(
                line(cash, EntrySide.DEBIT, "300.00"),
                line(loansReceivable, EntrySide.CREDIT, "300.00"));
        when(journalEntryRepository.findPostedEntriesTouchingCashAccounts(anyList(), eq(FROM), eq(TO)))
                .thenReturn(List.of(disbursement, repayment));

        CashFlowStatementResponse result = service.getCashFlowStatement(FROM, TO);

        assertThat(result.getOpeningCash()).isEqualByComparingTo("1500.00");
        assertThat(result.getTotalInflows()).isEqualByComparingTo("300.00");
        assertThat(result.getTotalOutflows()).isEqualByComparingTo("1000.00");
        assertThat(result.getNetCashMovement()).isEqualByComparingTo("-700.00");
        assertThat(result.getClosingCash()).isEqualByComparingTo("800.00");
        assertThat(result.getInflows()).singleElement()
                .satisfies(l -> assertThat(l.getAccountNo()).isEqualTo("1100"));
        assertThat(result.getOutflows()).singleElement()
                .satisfies(l -> assertThat(l.getAccountNo()).isEqualTo("1100"));
    }

    @Test
    void cashFlowIgnoresTransfersBetweenTwoCashAccounts() {
        GlAccount bank = account(7L, "1011", "Bank Account", AccountType.ASSET, EntrySide.DEBIT);
        ReflectionTestUtils.setField(service, "cashAccountNumbers", List.of("1010", "1011"));
        stubOpeningCash("5000.00", "0.00");
        // Moving money from the till to the bank changes neither the pool's total nor,
        // therefore, the statement — it must not show up as both an inflow and an outflow.
        JournalEntry sweep = entry(
                line(cash, EntrySide.CREDIT, "2000.00"),
                line(bank, EntrySide.DEBIT, "2000.00"));
        when(journalEntryRepository.findPostedEntriesTouchingCashAccounts(anyList(), eq(FROM), eq(TO)))
                .thenReturn(List.of(sweep));

        CashFlowStatementResponse result = service.getCashFlowStatement(FROM, TO);

        assertThat(result.getInflows()).isEmpty();
        assertThat(result.getOutflows()).isEmpty();
        assertThat(result.getNetCashMovement()).isEqualByComparingTo("0.00");
        assertThat(result.getClosingCash()).isEqualByComparingTo("5000.00");
    }

    @Test
    void cashFlowSplitsMultiLineEntriesProportionallyWithoutLosingCents() {
        stubOpeningCash("0.00", "0.00");
        // 10.00 of cash funded by three equal credits: 3.33 each leaves a cent over, which
        // must land somewhere or the statement stops reconciling to the cash account.
        JournalEntry entry = entry(
                line(cash, EntrySide.DEBIT, "10.00"),
                line(interestIncome, EntrySide.CREDIT, "3.34"),
                line(customerDeposits, EntrySide.CREDIT, "3.33"),
                line(loansReceivable, EntrySide.CREDIT, "3.33"));
        when(journalEntryRepository.findPostedEntriesTouchingCashAccounts(anyList(), eq(FROM), eq(TO)))
                .thenReturn(List.of(entry));

        CashFlowStatementResponse result = service.getCashFlowStatement(FROM, TO);

        assertThat(result.getTotalInflows()).isEqualByComparingTo("10.00");
        assertThat(result.getClosingCash()).isEqualByComparingTo("10.00");
        assertThat(result.getInflows()).hasSize(3);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void stubOpeningCash(String debit, String credit) {
        when(journalEntryLineRepository.aggregateForAccountNosBeforeDate(anyList(), eq(FROM))).thenReturn(List.of(
                new AccountSideTotal(cash.getId(), EntrySide.DEBIT, new BigDecimal(debit)),
                new AccountSideTotal(cash.getId(), EntrySide.CREDIT, new BigDecimal(credit))));
    }

    private void stubAccounts(GlAccount... accounts) {
        when(glAccountRepository.findAllById(any())).thenReturn(List.of(accounts));
    }

    private static AccountSideTotal total(GlAccount account, EntrySide side, String amount) {
        return new AccountSideTotal(account.getId(), side, new BigDecimal(amount));
    }

    private static GlAccount account(Long id, String no, String name, AccountType type, EntrySide normalBalance) {
        GlAccount account = GlAccount.builder()
                .accountNo(no)
                .accountName(name)
                .accountType(type)
                .normalBalance(normalBalance)
                .currency("USD")
                .build();
        account.setId(id);
        return account;
    }

    private static JournalEntryLine line(GlAccount account, EntrySide side, String amount) {
        return JournalEntryLine.builder()
                .glAccount(account)
                .entrySide(side)
                .amount(new BigDecimal(amount))
                .build();
    }

    private static JournalEntry entry(JournalEntryLine... lines) {
        JournalEntry entry = JournalEntry.builder().lines(new ArrayList<>(List.of(lines))).build();
        entry.getLines().forEach(l -> l.setJournalEntry(entry));
        return entry;
    }
}
