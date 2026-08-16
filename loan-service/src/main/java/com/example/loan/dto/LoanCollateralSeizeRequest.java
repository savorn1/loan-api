package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoanCollateralSeizeRequest {

    @NotBlank
    private String reason;
}
