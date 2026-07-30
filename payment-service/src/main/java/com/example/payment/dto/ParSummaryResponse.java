package com.example.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ParSummaryResponse {

    private List<ParBucketSummary> buckets;
    private BigDecimal totalOverdueAmount;
    private BigDecimal totalOutstandingBalance;
    private long activeLoanCount;
    // 0 when totalOutstandingBalance is 0 — avoids a divide-by-zero rather
    // than surfacing NaN/Infinity to the frontend.
    private BigDecimal portfolioAtRiskPercent;
}
