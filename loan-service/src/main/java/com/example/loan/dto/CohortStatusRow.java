package com.example.loan.dto;

import com.example.loan.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// One (disbursement-month, status) group from LoanRepository.aggregateVintageCohorts —
// pivoted into VintageCohortResponse (one row per month) by ReportServiceImpl.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CohortStatusRow {

    private String cohortMonth;
    private LoanStatus status;
    private long loanCount;
    private BigDecimal totalPrincipal;
}
