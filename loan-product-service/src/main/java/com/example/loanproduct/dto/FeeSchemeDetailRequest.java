package com.example.loanproduct.dto;

import com.example.loanproduct.entity.FeeCalculationMethod;
import com.example.loanproduct.entity.FeeChargeTiming;
import com.example.loanproduct.entity.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeSchemeDetailRequest {

    @NotNull
    private FeeType type;

    @NotNull
    private FeeCalculationMethod calculationMethod;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    @NotNull
    private FeeChargeTiming chargeTiming;
}
