package com.example.accounting.service.impl;

import com.example.accounting.client.LoanClient;
import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.LoanPortfolioSummaryResponse;
import com.example.accounting.dto.LoansReceivableReconciliationResponse;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.FinancialPeriodStatus;
import com.example.accounting.entity.GeneralLedger;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GeneralLedgerRepository;
import com.example.accounting.repository.GlAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// ReconciliationServiceImpl compares accounting-service's own GL control account (1100)
// against loan-service's independently-maintained portfolio total — the two are written by
// completely different code paths (JournalEntryServiceImpl.generate vs every LoanServiceImpl
// balance mutation), so nothing guarantees they agree without this check.
@ExtendWith(MockitoExtension.class)
class ReconciliationServiceImplTest {

    @Mock
    private GlAccountRepository glAccountRepository;
    @Mock
    private FinancialPeriodRepository financialPeriodRepository;
    @Mock
    private GeneralLedgerRepository generalLedgerRepository;
    @Mock
    private LoanClient loanClient;

    private ReconciliationServiceImpl service;

    private GlAccount receivableAccount;
    private FinancialPeriod openPeriod;

    private void setUp() {
        service = new ReconciliationServiceImpl(glAccountRepository, financialPeriodRepository,
                generalLedgerRepository, loanClient);

        receivableAccount = GlAccount.builder().accountNo("1100").allowPosting(true).build();
        receivableAccount.setId(7L);

        openPeriod = FinancialPeriod.builder()
                .periodName("2026-08")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .status(FinancialPeriodStatus.OPEN)
                .build();
        openPeriod.setId(1L);

        when(glAccountRepository.findByAccountNo("1100")).thenReturn(Optional.of(receivableAccount));
        when(financialPeriodRepository.findByDateWithinRange(any())).thenReturn(Optional.of(openPeriod));
    }

    private LoanPortfolioSummaryResponse portfolioTotal(BigDecimal total) {
        LoanPortfolioSummaryResponse response = new LoanPortfolioSummaryResponse();
        response.setTotalOutstandingBalance(total);
        return response;
    }

    @Test
    void reconcile_matchedWhenGlBalanceEqualsLoanServiceTotal() {
        setUp();
        GeneralLedger ledger = GeneralLedger.builder()
                .glAccount(receivableAccount).financialPeriod(openPeriod)
                .closingBalance(new BigDecimal("1000.00")).build();
        when(generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(7L, 1L)).thenReturn(Optional.of(ledger));
        when(loanClient.getPortfolioSummary()).thenReturn(ApiResponse.success(portfolioTotal(new BigDecimal("1000.00"))));

        LoansReceivableReconciliationResponse response = service.reconcileLoansReceivable();

        assertThat(response.isMatched()).isTrue();
        assertThat(response.getVariance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void reconcile_flagsVarianceWhenBalancesDiffer() {
        setUp();
        GeneralLedger ledger = GeneralLedger.builder()
                .glAccount(receivableAccount).financialPeriod(openPeriod)
                .closingBalance(new BigDecimal("1000.00")).build();
        when(generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(7L, 1L)).thenReturn(Optional.of(ledger));
        when(loanClient.getPortfolioSummary()).thenReturn(ApiResponse.success(portfolioTotal(new BigDecimal("2120.60"))));

        LoansReceivableReconciliationResponse response = service.reconcileLoansReceivable();

        assertThat(response.isMatched()).isFalse();
        assertThat(response.getVariance()).isEqualByComparingTo(new BigDecimal("-1120.60"));
    }

    @Test
    void reconcile_treatsNoGeneralLedgerRowAsZeroBalance() {
        setUp();
        when(generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(7L, 1L)).thenReturn(Optional.empty());
        when(loanClient.getPortfolioSummary()).thenReturn(ApiResponse.success(portfolioTotal(new BigDecimal("500.00"))));

        LoansReceivableReconciliationResponse response = service.reconcileLoansReceivable();

        assertThat(response.getGlBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getVariance()).isEqualByComparingTo(new BigDecimal("-500.00"));
    }

    @Test
    void reconcile_treatsNoOpenFinancialPeriodAsZeroBalance() {
        service = new ReconciliationServiceImpl(glAccountRepository, financialPeriodRepository,
                generalLedgerRepository, loanClient);
        receivableAccount = GlAccount.builder().accountNo("1100").allowPosting(true).build();
        receivableAccount.setId(7L);
        when(glAccountRepository.findByAccountNo("1100")).thenReturn(Optional.of(receivableAccount));
        when(financialPeriodRepository.findByDateWithinRange(any())).thenReturn(Optional.empty());
        when(loanClient.getPortfolioSummary()).thenReturn(ApiResponse.success(portfolioTotal(BigDecimal.ZERO)));

        LoansReceivableReconciliationResponse response = service.reconcileLoansReceivable();

        assertThat(response.getGlBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.isMatched()).isTrue();
    }
}
