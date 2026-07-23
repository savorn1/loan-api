package com.example.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRestructureRequest {

    @NotNull
    @Min(1)
    @Max(360)
    private Integer newTermMonths;

    // Optional — leave blank to keep the loan's current rate.
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private BigDecimal newInterestRate;

    @NotBlank
    private String reason;

    @NotNull
    private LocalDate effectiveDate;
}
