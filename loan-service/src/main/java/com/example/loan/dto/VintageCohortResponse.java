package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// Loan performance for one disbursement-month cohort. Only ACTIVE/CLOSED are broken
// out because disbursedAt (what the cohort is grouped by) is only set once a loan
// leaves PENDING/APPROVED/REJECTED and is actually disbursed.
@Data
@Builder
public class VintageCohortResponse {

    private String cohortMonth;
    private long loanCount;
    private BigDecimal totalPrincipal;
    private long activeCount;
    private BigDecimal activePrincipal;
    private long closedCount;
    private BigDecimal closedPrincipal;
}
