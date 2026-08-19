package com.example.loan.dto;

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

    @NotNull
    @Min(1)
    @Max(360)
    private Integer requestedTermMonths;

    private String purpose;
}
