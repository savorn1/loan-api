package com.example.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupLoanMemberRequest {

    @NotNull
    private Long customerId;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal requestedAmount;

    @NotNull
    @Min(1)
    @Max(360)
    private Integer requestedTermMonths;
}
