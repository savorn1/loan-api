package com.example.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanGuarantorRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    private String relationship;

    @DecimalMin("0.01")
    private BigDecimal guaranteedAmount;
}
