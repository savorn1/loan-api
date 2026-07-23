package com.example.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanSettlementRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal settlementAmount;

    @NotNull
    private LocalDate settlementDate;

    private String note;
}
