package com.example.accounting.service.impl;

import com.example.accounting.client.LoanClient;
import com.example.accounting.dto.LoansReceivableReconciliationResponse;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.exception.AppException;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GeneralLedgerRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

// Compares the GL's control account for loan receivables against loan-service's own
// subsidiary-ledger total (SUM of every Loan.outstandingBalance) — the two are maintained
// completely independently (one by JournalEntryServiceImpl posting, the other by every
// LoanServiceImpl balance mutation), so nothing today guarantees they agree. A variance
// means either a bug, a missed accounting call, or a manual journal entry that bypassed
// loan-service.
@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {

    // Same account the DISBURSEMENT/PRINCIPAL_PAYMENT templates post to — see
    // JournalTemplateSeeder and GlAccountSeeder. Hardcoded rather than resolved through a
    // template/scheme lookup because this check is specifically "does 1100 agree with
    // loan-service", not "whatever role happens to be configured right now".
    private static final String LOANS_RECEIVABLE_ACCOUNT_NO = "1100";

    private final GlAccountRepository glAccountRepository;
    private final FinancialPeriodRepository financialPeriodRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final LoanClient loanClient;

    @Override
    public LoansReceivableReconciliationResponse reconcileLoansReceivable() {
        GlAccount account = glAccountRepository.findByAccountNo(LOANS_RECEIVABLE_ACCOUNT_NO)
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Loans Receivable account " + LOANS_RECEIVABLE_ACCOUNT_NO + " is not configured"));

        BigDecimal glBalance = financialPeriodRepository.findByDateWithinRange(LocalDate.now())
                .map(FinancialPeriod::getId)
                .flatMap(periodId -> generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(account.getId(), periodId))
                .map(gl -> gl.getClosingBalance())
                .orElse(BigDecimal.ZERO);

        BigDecimal loanServiceTotal = loanClient.getPortfolioSummary().getData().getTotalOutstandingBalance();
        if (loanServiceTotal == null) {
            loanServiceTotal = BigDecimal.ZERO;
        }

        BigDecimal variance = glBalance.subtract(loanServiceTotal);
        return LoansReceivableReconciliationResponse.builder()
                .glAccountNo(account.getAccountNo())
                .glBalance(glBalance)
                .loanServiceOutstandingTotal(loanServiceTotal)
                .variance(variance)
                .matched(variance.compareTo(BigDecimal.ZERO) == 0)
                .build();
    }
}
