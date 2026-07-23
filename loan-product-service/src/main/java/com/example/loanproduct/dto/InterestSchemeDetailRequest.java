package com.example.loanproduct.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InterestSchemeDetailRequest {

    @NotNull
    @Min(1)
    private Integer minTerm;

    @NotNull
    @Min(1)
    private Integer maxTerm;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal maxAmount;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal interestRate;
}
