package com.example.accounting.service;

import com.example.accounting.dto.BudgetVsActualRow;
import com.example.accounting.dto.IncomeByBranchResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    IncomeByBranchResponse getIncomeByBranch(LocalDate dateFrom, LocalDate dateTo);

    List<BudgetVsActualRow> getBudgetVsActual(Long financialPeriodId);
}
