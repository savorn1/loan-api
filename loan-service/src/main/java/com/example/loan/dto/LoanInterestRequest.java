package com.example.loan.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanInterestRequest {

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private BigDecimal rate;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
