package com.example.accounting.service.impl;

import com.example.accounting.client.LoanClient;
import com.example.accounting.dto.LoansReceivableReconciliationResponse;
import com.example.accounting.dto.ReconciliationPostingResponse;
import com.example.accounting.dto.ReconciliationSnapshotResponse;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.entity.JournalEntryLine;
import com.example.accounting.entity.ReconciliationSnapshot;
import com.example.accounting.exception.AppException;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GeneralLedgerRepository;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalEntryLineRepository;
import com.example.accounting.repository.ReconciliationSnapshotRepository;
import com.example.accounting.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final ReconciliationSnapshotRepository reconciliationSnapshotRepository;
    private final LoanClient loanClient;

    @Override
    public LoansReceivableReconciliationResponse reconcileLoansReceivable() {
        GlAccount account = findLoansReceivableAccount();
        BigDecimal glBalance = currentGlBalance(account);
        BigDecimal loanServiceTotal = currentLoanServiceTotal();
        BigDecimal variance = glBalance.subtract(loanServiceTotal);

        return LoansReceivableReconciliationResponse.builder()
                .glAccountNo(account.getAccountNo())
                .glBalance(glBalance)
                .loanServiceOutstandingTotal(loanServiceTotal)
                .variance(variance)
                .matched(variance.compareTo(BigDecimal.ZERO) == 0)
                .build();
    }

    @Override
    public ReconciliationSnapshotResponse takeSnapshot() {
        LoansReceivableReconciliationResponse result = reconcileLoansReceivable();
        ReconciliationSnapshot snapshot = reconciliationSnapshotRepository.save(ReconciliationSnapshot.builder()
                .glAccountNo(result.getGlAccountNo())
                .glBalance(result.getGlBalance())
                .loanServiceOutstandingTotal(result.getLoanServiceOutstandingTotal())
                .variance(result.getVariance())
                .matched(result.isMatched())
                .build());
        return toSnapshotResponse(snapshot);
    }

    @Override
    public List<ReconciliationSnapshotResponse> getHistory() {
        return reconciliationSnapshotRepository.findTop90ByOrderByCreatedAtDesc().stream()
                .map(this::toSnapshotResponse)
                .toList();
    }

    @Override
    public List<ReconciliationPostingResponse> getPostings() {
        GlAccount account = findLoansReceivableAccount();
        Optional<FinancialPeriod> period = financialPeriodRepository.findByDateWithinRange(LocalDate.now());
        if (period.isEmpty()) {
            return List.of();
        }
        return journalEntryLineRepository
                .findPostedLinesForAccountAndPeriod(account.getId(), period.get().getId())
                .stream()
                .map(this::toPostingResponse)
                .toList();
    }

    private GlAccount findLoansReceivableAccount() {
        return glAccountRepository.findByAccountNo(LOANS_RECEIVABLE_ACCOUNT_NO)
                .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Loans Receivable account " + LOANS_RECEIVABLE_ACCOUNT_NO + " is not configured"));
    }

    private BigDecimal currentGlBalance(GlAccount account) {
        return financialPeriodRepository.findByDateWithinRange(LocalDate.now())
                .map(FinancialPeriod::getId)
                .flatMap(periodId -> generalLedgerRepository.findByGlAccountIdAndFinancialPeriodId(account.getId(), periodId))
                .map(gl -> gl.getClosingBalance())
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal currentLoanServiceTotal() {
        BigDecimal total = loanClient.getPortfolioSummary().getData().getTotalOutstandingBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    private ReconciliationSnapshotResponse toSnapshotResponse(ReconciliationSnapshot snapshot) {
        return ReconciliationSnapshotResponse.builder()
                .id(snapshot.getId())
                .checkedAt(snapshot.getCreatedAt())
                .glAccountNo(snapshot.getGlAccountNo())
                .glBalance(snapshot.getGlBalance())
                .loanServiceOutstandingTotal(snapshot.getLoanServiceOutstandingTotal())
                .variance(snapshot.getVariance())
                .matched(snapshot.isMatched())
                .build();
    }

    private ReconciliationPostingResponse toPostingResponse(JournalEntryLine line) {
        return ReconciliationPostingResponse.builder()
                .entryNo(line.getJournalEntry().getEntryNo())
                .transactionDate(line.getJournalEntry().getTransactionDate())
                .transactionType(line.getJournalEntry().getTransactionType())
                .referenceType(line.getJournalEntry().getReferenceType())
                .referenceId(line.getJournalEntry().getReferenceId())
                .entrySide(line.getEntrySide())
                .amount(line.getAmount())
                .description(line.getDescription())
                .build();
    }
}
