package com.example.loan.dto;

import com.example.loan.entity.TermUnit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRestructureRequest {

    // Interpreted per newTermUnit — see Loan.termMonths. Bound relaxed from
    // the old month-only @Max(360) since this can now also hold a day count
    // (unit-aware validation isn't enforced here yet).
    @NotNull
    @Min(1)
    @Max(3650)
    private Integer newTermMonths;

    @NotNull
    private TermUnit newTermUnit;

    // Optional — leave blank to keep the loan's current rate.
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private BigDecimal newInterestRate;

    @NotBlank
    private String reason;

    @NotNull
    private LocalDate effectiveDate;
}
