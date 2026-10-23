package com.example.accounting.service.impl;

import com.example.accounting.dto.AccountSideTotal;
import com.example.accounting.dto.BalanceSheetResponse;
import com.example.accounting.dto.CashFlowLine;
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
import com.example.accounting.service.FinancialStatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialStatementServiceImpl implements FinancialStatementService {

    private static final int MONEY_SCALE = 2;

    private final JournalEntryLineRepository journalEntryLineRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final GlAccountRepository glAccountRepository;

    /**
     * Which GL accounts the cash flow statement treats as "cash". Defaults to the
     * account number {@code GlAccountSeeder} creates for Cash and Cash Equivalents;
     * override when the chart of accounts grows more (bank accounts, mobile money
     * float, petty cash) so they're all counted as one pool.
     */
    @Value("${accounting.cash-account-numbers:1010}")
    private List<String> cashAccountNumbers;

    @Override
    @Transactional(readOnly = true)
    public IncomeStatementResponse getIncomeStatement(LocalDate dateFrom, LocalDate dateTo) {
        Map<AccountType, List<FinancialStatementLine>> sections =
                sectionsFor(journalEntryLineRepository.aggregateByAccountAndSideForDateRange(dateFrom, dateTo));

        List<FinancialStatementLine> income = sections.getOrDefault(AccountType.INCOME, List.of());
        List<FinancialStatementLine> expenses = sections.getOrDefault(AccountType.EXPENSE, List.of());
        BigDecimal totalIncome = total(income);
        BigDecimal totalExpenses = total(expenses);

        return IncomeStatementResponse.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .income(income)
                .expenses(expenses)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netIncome(totalIncome.subtract(totalExpenses))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetResponse getBalanceSheet(LocalDate asOfDate) {
        // dateFrom = null: a balance sheet is cumulative from inception, not period-scoped.
        Map<AccountType, List<FinancialStatementLine>> sections =
                sectionsFor(journalEntryLineRepository.aggregateByAccountAndSideForDateRange(null, asOfDate));

        List<FinancialStatementLine> assets = sections.getOrDefault(AccountType.ASSET, List.of());
        List<FinancialStatementLine> liabilities = sections.getOrDefault(AccountType.LIABILITY, List.of());
        List<FinancialStatementLine> equity = sections.getOrDefault(AccountType.EQUITY, List.of());

        BigDecimal totalAssets = total(assets);
        BigDecimal totalLiabilities = total(liabilities);
        BigDecimal totalEquity = total(equity);
        // P&L is never closed into equity by this system (closing a period only flips a
        // status flag), so the cumulative net has to be carried here for the sheet to balance.
        BigDecimal currentEarnings = total(sections.getOrDefault(AccountType.INCOME, List.of()))
                .subtract(total(sections.getOrDefault(AccountType.EXPENSE, List.of())));
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity).add(currentEarnings);

        return BalanceSheetResponse.builder()
                .asOfDate(asOfDate)
                .assets(assets)
                .liabilities(liabilities)
                .equity(equity)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .currentEarnings(currentEarnings)
                .totalLiabilitiesAndEquity(totalLiabilitiesAndEquity)
                .balanced(totalAssets.compareTo(totalLiabilitiesAndEquity) == 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CashFlowStatementResponse getCashFlowStatement(LocalDate dateFrom, LocalDate dateTo) {
        Set<String> cashAccountNos = Set.copyOf(cashAccountNumbers);
        BigDecimal openingCash = openingCashBalance(dateFrom);

        Map<Long, CashFlowLine> inflows = new LinkedHashMap<>();
        Map<Long, CashFlowLine> outflows = new LinkedHashMap<>();

        for (JournalEntry entry : journalEntryRepository
                .findPostedEntriesTouchingCashAccounts(cashAccountNumbers, dateFrom, dateTo)) {
            List<JournalEntryLine> cashLines = entry.getLines().stream()
                    .filter(l -> cashAccountNos.contains(l.getGlAccount().getAccountNo()))
                    .toList();
            BigDecimal netCash = signedTotal(cashLines);
            // Zero means the entry only shuffled cash between two cash accounts — the pool
            // as a whole didn't change, so it's neither an inflow nor an outflow.
            if (netCash.signum() == 0) {
                continue;
            }

            boolean isInflow = netCash.signum() > 0;
            // Cash in was funded by the credits; cash out was spent on the debits.
            EntrySide counterpartSide = isInflow ? EntrySide.CREDIT : EntrySide.DEBIT;
            List<JournalEntryLine> counterparts = entry.getLines().stream()
                    .filter(l -> !cashAccountNos.contains(l.getGlAccount().getAccountNo()))
                    .filter(l -> l.getEntrySide() == counterpartSide)
                    .toList();

            allocate(netCash.abs(), counterparts, isInflow ? inflows : outflows);
        }

        List<CashFlowLine> inflowLines = sortedByAmountDesc(inflows.values());
        List<CashFlowLine> outflowLines = sortedByAmountDesc(outflows.values());
        BigDecimal totalInflows = totalCash(inflowLines);
        BigDecimal totalOutflows = totalCash(outflowLines);
        BigDecimal netCashMovement = totalInflows.subtract(totalOutflows);

        return CashFlowStatementResponse.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .openingCash(openingCash)
                .inflows(inflowLines)
                .outflows(outflowLines)
                .totalInflows(totalInflows)
                .totalOutflows(totalOutflows)
                .netCashMovement(netCashMovement)
                .closingCash(openingCash.add(netCashMovement))
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Turns raw (account, side, total) rows into per-account statement lines grouped by
     * account type, with each amount normalized to its account's normal balance — the
     * same rule {@code TrialBalanceServiceImpl} applies, so both reports agree.
     */
    private Map<AccountType, List<FinancialStatementLine>> sectionsFor(List<AccountSideTotal> totals) {
        Map<Long, List<AccountSideTotal>> byAccount = totals.stream()
                .collect(Collectors.groupingBy(AccountSideTotal::glAccountId));
        Map<Long, GlAccount> accounts = glAccountRepository.findAllById(byAccount.keySet()).stream()
                .collect(Collectors.toMap(GlAccount::getId, a -> a));

        Map<AccountType, List<FinancialStatementLine>> sections = new EnumMap<>(AccountType.class);
        byAccount.forEach((accountId, accountTotals) -> {
            GlAccount account = accounts.get(accountId);
            // An account deleted after its lines were posted would orphan them; skipping is
            // safer than failing the whole statement, and the trial balance still shows it.
            if (account == null) {
                return;
            }
            BigDecimal debit = sumSide(accountTotals, EntrySide.DEBIT);
            BigDecimal credit = sumSide(accountTotals, EntrySide.CREDIT);
            BigDecimal amount = account.getNormalBalance() == EntrySide.DEBIT
                    ? debit.subtract(credit)
                    : credit.subtract(debit);
            sections.computeIfAbsent(account.getAccountType(), t -> new ArrayList<>())
                    .add(FinancialStatementLine.builder()
                            .glAccountId(account.getId())
                            .accountNo(account.getAccountNo())
                            .accountName(account.getAccountName())
                            .amount(amount)
                            .build());
        });
        sections.values().forEach(lines -> lines.sort(Comparator.comparing(FinancialStatementLine::getAccountNo)));
        return sections;
    }

    private BigDecimal openingCashBalance(LocalDate dateFrom) {
        List<AccountSideTotal> totals =
                journalEntryLineRepository.aggregateForAccountNosBeforeDate(cashAccountNumbers, dateFrom);
        // Cash accounts are assets (debit-normal), so the balance is debits less credits.
        return sumSide(totals, EntrySide.DEBIT).subtract(sumSide(totals, EntrySide.CREDIT));
    }

    /** Debits less credits across the given lines. */
    private BigDecimal signedTotal(List<JournalEntryLine> lines) {
        return lines.stream()
                .map(l -> l.getEntrySide() == EntrySide.DEBIT ? l.getAmount() : l.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Splits one entry's cash movement across the accounts that funded or consumed it,
     * in proportion to their amounts, and folds the result into {@code target}.
     *
     * <p>For the two-line entries the journal templates generate this is just "all of it
     * to the one counterpart". For hand-written multi-line entries the proportional split
     * is an apportionment, not a fact the ledger records — the rounding remainder is
     * pushed onto the largest share so the statement still reconciles to the cash
     * accounts exactly.
     */
    private void allocate(BigDecimal cashAmount, List<JournalEntryLine> counterparts, Map<Long, CashFlowLine> target) {
        if (counterparts.isEmpty()) {
            // Only reachable if an entry is unbalanced or cash-only in a way that didn't net
            // to zero; bucket it rather than silently dropping cash the ledger did move.
            merge(target, null, "—", "Unclassified", cashAmount);
            return;
        }

        BigDecimal counterpartTotal = counterparts.stream()
                .map(JournalEntryLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (counterpartTotal.signum() == 0) {
            merge(target, null, "—", "Unclassified", cashAmount);
            return;
        }

        Map<JournalEntryLine, BigDecimal> shares = new HashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (JournalEntryLine line : counterparts) {
            BigDecimal share = cashAmount.multiply(line.getAmount())
                    .divide(counterpartTotal, MONEY_SCALE, RoundingMode.HALF_UP);
            shares.put(line, share);
            allocated = allocated.add(share);
        }

        BigDecimal remainder = cashAmount.subtract(allocated);
        if (remainder.signum() != 0) {
            JournalEntryLine largest = counterparts.stream()
                    .max(Comparator.comparing(JournalEntryLine::getAmount))
                    .orElseThrow();
            shares.merge(largest, remainder, BigDecimal::add);
        }

        shares.forEach((line, amount) -> {
            GlAccount account = line.getGlAccount();
            merge(target, account.getId(), account.getAccountNo(), account.getAccountName(), amount);
        });
    }

    private void merge(Map<Long, CashFlowLine> target, Long accountId, String accountNo, String accountName,
                       BigDecimal amount) {
        CashFlowLine existing = target.get(accountId);
        if (existing == null) {
            target.put(accountId, CashFlowLine.builder()
                    .glAccountId(accountId)
                    .accountNo(accountNo)
                    .accountName(accountName)
                    .amount(amount)
                    .build());
        } else {
            existing.setAmount(existing.getAmount().add(amount));
        }
    }

    private List<CashFlowLine> sortedByAmountDesc(java.util.Collection<CashFlowLine> lines) {
        return lines.stream()
                .sorted(Comparator.comparing(CashFlowLine::getAmount).reversed())
                .toList();
    }

    private BigDecimal sumSide(List<AccountSideTotal> totals, EntrySide side) {
        return totals.stream()
                .filter(t -> t.entrySide() == side)
                .map(AccountSideTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal total(List<FinancialStatementLine> lines) {
        return lines.stream().map(FinancialStatementLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalCash(List<CashFlowLine> lines) {
        return lines.stream().map(CashFlowLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
