package com.example.loan.dto;

import com.example.loan.entity.LoanFeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanFeeRequest {

    @NotNull
    private LoanFeeType type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate chargedDate;

    private String description;
}
