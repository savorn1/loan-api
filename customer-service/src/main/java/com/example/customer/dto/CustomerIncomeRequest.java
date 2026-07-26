package com.example.customer.dto;

import com.example.customer.entity.IncomeFrequency;
import com.example.customer.entity.IncomeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerIncomeRequest {

    @NotNull
    private IncomeType incomeType;

    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal amount;

    private String currency;

    @NotNull
    private IncomeFrequency frequency;
}
