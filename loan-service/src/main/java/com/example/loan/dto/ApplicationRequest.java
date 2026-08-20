package com.example.loan.dto;

import com.example.loan.entity.TermUnit;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApplicationRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private UUID loanProductId;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal requestedAmount;

    // Interpreted per requestedTermUnit — see Application.requestedTermMonths.
    // Bound relaxed from the old month-only @Max(360) since this can now also
    // hold a day count (unit-aware validation isn't enforced here yet).
    @NotNull
    @Min(1)
    @Max(3650)
    private Integer requestedTermMonths;

    @NotNull
    private TermUnit requestedTermUnit;

    private String purpose;
}
