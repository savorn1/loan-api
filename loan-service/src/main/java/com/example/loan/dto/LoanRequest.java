package com.example.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequest {

    @NotNull
    private Long customerId;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal principal;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private BigDecimal interestRate;

    @NotNull
    @Min(1)
    @Max(360)
    private Integer termMonths;

    private String purpose;
}
