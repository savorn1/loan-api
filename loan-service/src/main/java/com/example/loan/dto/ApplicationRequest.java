package com.example.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicationRequest {

    @NotNull
    private Long customerId;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal requestedAmount;

    @NotNull
    @Min(1)
    @Max(360)
    private Integer requestedTermMonths;

    private String purpose;
}
