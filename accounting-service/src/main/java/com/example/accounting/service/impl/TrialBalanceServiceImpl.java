package com.example.accounting.service.impl;

import com.example.accounting.dto.AccountSideTotal;
import com.example.accounting.dto.TrialBalanceRowResponse;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.GlAccount;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.GlAccountRepository;
import com.example.accounting.repository.JournalEntryLineRepository;
import com.example.accounting.service.TrialBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrialBalanceServiceImpl implements TrialBalanceService {

    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GlAccountRepository glAccountRepository;

    @Override
    public List<TrialBalanceRowResponse> getTrialBalance(Long financialPeriodId) {
        List<AccountSideTotal> totals = journalEntryLineRepository.aggregateByAccountAndSideForPeriod(financialPeriodId);
        Map<Long, List<AccountSideTotal>> byAccount = totals.stream()
                .collect(Collectors.groupingBy(AccountSideTotal::glAccountId));

        List<TrialBalanceRowResponse> rows = new java.util.ArrayList<>();
        for (Map.Entry<Long, List<AccountSideTotal>> entry : byAccount.entrySet()) {
            GlAccount account = glAccountRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("GL account", entry.getKey()));
            BigDecimal debitTotal = sumSide(entry.getValue(), EntrySide.DEBIT);
            BigDecimal creditTotal = sumSide(entry.getValue(), EntrySide.CREDIT);
            BigDecimal balance = account.getNormalBalance() == EntrySide.DEBIT
                    ? debitTotal.subtract(creditTotal)
                    : creditTotal.subtract(debitTotal);
            rows.add(TrialBalanceRowResponse.builder()
                    .glAccountId(account.getId())
                    .accountNo(account.getAccountNo())
                    .accountName(account.getAccountName())
                    .totalDebit(debitTotal)
                    .totalCredit(creditTotal)
                    .balance(balance)
                    .build());
        }
        return rows.stream().sorted(Comparator.comparing(TrialBalanceRowResponse::getAccountNo)).toList();
    }

    private BigDecimal sumSide(List<AccountSideTotal> totals, EntrySide side) {
        return totals.stream()
                .filter(t -> t.entrySide() == side)
                .map(AccountSideTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
