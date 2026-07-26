package com.example.customer.dto;

import com.example.customer.entity.AmlStatus;
import com.example.customer.entity.RiskLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRiskProfileRequest {

    @NotNull
    private RiskLevel riskLevel;

    private boolean pep;
    private boolean sanctionChecked;

    @NotNull
    private AmlStatus amlStatus;

    private LocalDate lastReviewDate;
}
