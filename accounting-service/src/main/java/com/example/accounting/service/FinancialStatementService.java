package com.example.accounting.service;

import com.example.accounting.dto.BalanceSheetResponse;
import com.example.accounting.dto.CashFlowStatementResponse;
import com.example.accounting.dto.IncomeStatementResponse;

import java.time.LocalDate;

/**
 * The three primary financial statements, computed from posted journal entry lines —
 * nothing is stored. Companion to {@link TrialBalanceService}, which these share their
 * normal-balance convention with so the numbers always agree with the trial balance.
 */
public interface FinancialStatementService {

    IncomeStatementResponse getIncomeStatement(LocalDate dateFrom, LocalDate dateTo);

    BalanceSheetResponse getBalanceSheet(LocalDate asOfDate);

    CashFlowStatementResponse getCashFlowStatement(LocalDate dateFrom, LocalDate dateTo);
}
