package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Profit &amp; loss for a date range: every INCOME and EXPENSE account that had posted
 * activity in the window, plus the totals. Unlike the balance sheet this is a
 * *period* statement — only entries dated inside the range are counted.
 */
@Data
@Builder
public class IncomeStatementResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<FinancialStatementLine> income;
    private List<FinancialStatementLine> expenses;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netIncome;
}
