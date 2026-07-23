package com.example.loan.dto;

import com.example.loan.entity.CollateralType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanCollateralRequest {

    @NotNull
    private CollateralType type;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal estimatedValue;

    private String reference;
}
