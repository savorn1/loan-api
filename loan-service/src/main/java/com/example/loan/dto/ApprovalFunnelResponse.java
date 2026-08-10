package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ApprovalFunnelResponse {

    private long totalSubmitted;
    private long totalApproved;
    private long totalRejected;
    // 0 when totalSubmitted is 0 — avoids a divide-by-zero rather than surfacing
    // NaN/Infinity to the frontend (same convention as ParSummaryResponse.portfolioAtRiskPercent).
    private BigDecimal approvalRatePercent;
    // Average calendar days between submittedAt and decidedAt, across applications
    // that have been decided (APPROVED/REJECTED) within the window. Null when none have.
    private BigDecimal avgDecisionDays;
    private List<ApplicationStatusCount> statusBreakdown;
}
