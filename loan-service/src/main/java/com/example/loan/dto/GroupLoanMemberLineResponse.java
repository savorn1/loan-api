package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GroupLoanMemberLineResponse {

    private Long customerId;
    private String customerName;
    private BigDecimal requestedAmount;
    private Integer requestedTermMonths;
    private BigDecimal approvedAmount;
    private BigDecimal approvedInterestRate;
    private Integer approvedTermMonths;
    private Long loanId;
}
