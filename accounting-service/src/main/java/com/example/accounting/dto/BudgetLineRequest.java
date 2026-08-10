package com.example.accounting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetLineRequest {

    @NotNull
    private Long financialPeriodId;

    @NotNull
    private Long glAccountId;

    @NotNull
    private BigDecimal budgetAmount;
}
