package com.example.accounting.service.impl;

import com.example.accounting.dto.TrialBalanceResponse;
import com.example.accounting.dto.TrialBalanceRowResponse;
import com.example.accounting.entity.FinancialPeriod;
import com.example.accounting.entity.GeneralLedger;
import com.example.accounting.entity.TrialBalance;
import com.example.accounting.entity.TrialBalanceLine;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.FinancialPeriodRepository;
import com.example.accounting.repository.GeneralLedgerRepository;
import com.example.accounting.repository.TrialBalanceRepository;
import com.example.accounting.service.TrialBalanceSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrialBalanceSnapshotServiceImpl implements TrialBalanceSnapshotService {

    private final TrialBalanceRepository trialBalanceRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final FinancialPeriodRepository financialPeriodRepository;

    @Override
    @Transactional
    public TrialBalanceResponse generate(Long financialPeriodId) {
        FinancialPeriod period = financialPeriodRepository.findById(financialPeriodId)
                .orElseThrow(() -> new ResourceNotFoundException("Financial period", financialPeriodId));

        // Sourced from the general_ledger balance table rather than re-aggregating
        // journal_entry_lines — see GeneralLedgerServiceImpl. Only accounts that already have
        // a general_ledger row for this period appear here; an account with a carried-forward
        // balance but zero activity in this specific period won't show up until something
        // posts against it again (rows are created lazily on posting, not eagerly per period).
        List<GeneralLedger> ledgerRows = generalLedgerRepository.findByFinancialPeriodId(financialPeriodId);

        TrialBalance snapshot = TrialBalance.builder()
                .financialPeriod(period)
                .generatedAt(LocalDateTime.now())
                .generatedBy(currentUsername())
                .build();
        ledgerRows.stream()
                .map(row -> TrialBalanceLine.builder()
                        .trialBalance(snapshot)
                        .glAccount(row.getGlAccount())
                        .totalDebit(row.getPeriodDebitTotal())
                        .totalCredit(row.getPeriodCreditTotal())
                        .balance(row.getClosingBalance())
                        .build())
                .forEach(snapshot.getLines()::add);

        return toResponse(trialBalanceRepository.save(snapshot));
    }

    @Override
    public TrialBalanceResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<TrialBalanceResponse> getAll(Long financialPeriodId) {
        return trialBalanceRepository.findByFinancialPeriodIdOrderByGeneratedAtDesc(financialPeriodId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TrialBalance findOrThrow(Long id) {
        return trialBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trial balance snapshot", id));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private TrialBalanceResponse toResponse(TrialBalance snapshot) {
        List<TrialBalanceRowResponse> lines = snapshot.getLines().stream()
                .map(l -> TrialBalanceRowResponse.builder()
                        .glAccountId(l.getGlAccount().getId())
                        .accountNo(l.getGlAccount().getAccountNo())
                        .accountName(l.getGlAccount().getAccountName())
                        .totalDebit(l.getTotalDebit())
                        .totalCredit(l.getTotalCredit())
                        .balance(l.getBalance())
                        .build())
                .sorted((a, b) -> a.getAccountNo().compareTo(b.getAccountNo()))
                .toList();
        BigDecimal totalDebit = lines.stream().map(TrialBalanceRowResponse::getTotalDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(TrialBalanceRowResponse::getTotalCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        return TrialBalanceResponse.builder()
                .id(snapshot.getId())
                .financialPeriodId(snapshot.getFinancialPeriod().getId())
                .financialPeriodName(snapshot.getFinancialPeriod().getPeriodName())
                .generatedAt(snapshot.getGeneratedAt())
                .generatedBy(snapshot.getGeneratedBy())
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .lines(lines)
                .build();
    }
}
