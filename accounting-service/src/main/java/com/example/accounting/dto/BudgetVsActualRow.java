package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetVsActualRow {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal variance;
    // 0 when budgetAmount is 0 — avoids a divide-by-zero rather than surfacing
    // NaN/Infinity to the frontend (same convention as ParSummaryResponse.portfolioAtRiskPercent).
    private BigDecimal variancePercent;
}
