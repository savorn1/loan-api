package com.example.accounting.service.impl;

import com.example.accounting.dto.BranchAccountTypeTotal;
import com.example.accounting.dto.BudgetVsActualRow;
import com.example.accounting.dto.IncomeByBranchResponse;
import com.example.accounting.dto.IncomeByBranchRow;
import com.example.accounting.dto.TrialBalanceRowResponse;
import com.example.accounting.entity.AccountType;
import com.example.accounting.entity.BudgetLine;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.repository.BudgetLineRepository;
import com.example.accounting.repository.JournalEntryLineRepository;
import com.example.accounting.service.ReportService;
import com.example.accounting.service.TrialBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final JournalEntryLineRepository journalEntryLineRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final TrialBalanceService trialBalanceService;

    // Pivots the (branch, accountType, entrySide) totals into one row per branch —
    // income's normal balance is CREDIT so net income there is credit minus debit
    // (the debit side only ever holds reversals/corrections); expense's normal
    // balance is DEBIT so it's the mirror image.
    @Override
    public IncomeByBranchResponse getIncomeByBranch(LocalDate dateFrom, LocalDate dateTo) {
        List<BranchAccountTypeTotal> totals = journalEntryLineRepository.aggregateIncomeExpenseByBranch(dateFrom, dateTo);
        // journalEntry.branchId is nullable (e.g. head-office/company-level entries with
        // no branch attached) — Collectors.groupingBy can't accept a null key, and those
        // entries don't belong in a *by-branch* breakdown anyway, so they're excluded here.
        Map<Long, List<BranchAccountTypeTotal>> byBranch = totals.stream()
                .filter(t -> t.branchId() != null)
                .collect(Collectors.groupingBy(BranchAccountTypeTotal::branchId));

        List<IncomeByBranchRow> rows = byBranch.entrySet().stream()
                .map(entry -> {
                    List<BranchAccountTypeTotal> branchTotals = entry.getValue();
                    BigDecimal incomeCredit = sum(branchTotals, AccountType.INCOME, EntrySide.CREDIT);
                    BigDecimal incomeDebit = sum(branchTotals, AccountType.INCOME, EntrySide.DEBIT);
                    BigDecimal expenseDebit = sum(branchTotals, AccountType.EXPENSE, EntrySide.DEBIT);
                    BigDecimal expenseCredit = sum(branchTotals, AccountType.EXPENSE, EntrySide.CREDIT);
                    BigDecimal totalIncome = incomeCredit.subtract(incomeDebit);
                    BigDecimal totalExpense = expenseDebit.subtract(expenseCredit);
                    return IncomeByBranchRow.builder()
                            .branchId(entry.getKey())
                            .totalIncome(totalIncome)
                            .totalExpense(totalExpense)
                            .netIncome(totalIncome.subtract(totalExpense))
                            .build();
                })
                .sorted((a, b) -> b.getNetIncome().compareTo(a.getNetIncome()))
                .toList();

        return IncomeByBranchResponse.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .rows(rows)
                .build();
    }

    private BigDecimal sum(List<BranchAccountTypeTotal> totals, AccountType type, EntrySide side) {
        return totals.stream()
                .filter(t -> t.accountType() == type && t.entrySide() == side)
                .map(BranchAccountTypeTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Reuses TrialBalanceService's per-account balance (already normalized for each
    // account's normal-balance direction) as "actual" — a budget line's target
    // account is compared against exactly the same number the Trial Balance report
    // would show for that account and period, so the two reports never disagree.
    @Override
    public List<BudgetVsActualRow> getBudgetVsActual(Long financialPeriodId) {
        List<BudgetLine> budgetLines = budgetLineRepository.findByFinancialPeriodId(financialPeriodId);
        Map<Long, TrialBalanceRowResponse> actualByAccount = trialBalanceService.getTrialBalance(financialPeriodId).stream()
                .collect(Collectors.toMap(TrialBalanceRowResponse::getGlAccountId, row -> row));

        return budgetLines.stream()
                .map(line -> {
                    Long glAccountId = line.getGlAccount().getId();
                    TrialBalanceRowResponse actualRow = actualByAccount.get(glAccountId);
                    BigDecimal actual = actualRow != null ? actualRow.getBalance() : BigDecimal.ZERO;
                    BigDecimal budget = line.getBudgetAmount();
                    BigDecimal variance = actual.subtract(budget);
                    BigDecimal variancePercent = budget.compareTo(BigDecimal.ZERO) != 0
                            ? variance.multiply(BigDecimal.valueOf(100)).divide(budget, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return BudgetVsActualRow.builder()
                            .glAccountId(glAccountId)
                            .accountNo(line.getGlAccount().getAccountNo())
                            .accountName(line.getGlAccount().getAccountName())
                            .budgetAmount(budget)
                            .actualAmount(actual)
                            .variance(variance)
                            .variancePercent(variancePercent)
                            .build();
                })
                .sorted((a, b) -> a.getAccountNo().compareTo(b.getAccountNo()))
                .toList();
    }
}
