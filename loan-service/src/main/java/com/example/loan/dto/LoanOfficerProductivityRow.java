package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// One row per loan officer (auth-service SysUser, cross-service id — no name/user
// details resolved here; the frontend joins against /auth/users the same way it
// already does for loan/customer numbers elsewhere in the reports catalog).
@Data
@Builder
public class LoanOfficerProductivityRow {

    private Long loanOfficerId;
    private long loanCount;
    private BigDecimal totalPrincipal;
    private long activeLoanCount;
    private BigDecimal activeOutstandingBalance;
}
