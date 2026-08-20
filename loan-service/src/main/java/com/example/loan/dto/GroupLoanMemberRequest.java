package com.example.loan.dto;

import com.example.loan.entity.TermUnit;
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

    // Interpreted per requestedTermUnit. Bound relaxed from the old
    // month-only @Max(360) since this can now also hold a day count
    // (unit-aware validation isn't enforced here yet).
    @NotNull
    @Min(1)
    @Max(3650)
    private Integer requestedTermMonths;

    @NotNull
    private TermUnit requestedTermUnit;
}
