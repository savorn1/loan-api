package com.example.loan.dto;

import com.example.loan.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusBreakdownResponse {

    private LoanStatus status;
    private long loanCount;
    private BigDecimal totalPrincipal;
}
