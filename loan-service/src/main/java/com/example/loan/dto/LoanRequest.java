package com.example.loan.dto;

import com.example.loan.entity.TermUnit;
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

    // Interpreted per termUnit — see Loan.termMonths. Bound relaxed from the
    // old month-only @Max(360) since this can now also hold a day count
    // (unit-aware validation isn't enforced here yet).
    @NotNull
    @Min(1)
    @Max(3650)
    private Integer termMonths;

    @NotNull
    private TermUnit termUnit;

    private String purpose;
}
