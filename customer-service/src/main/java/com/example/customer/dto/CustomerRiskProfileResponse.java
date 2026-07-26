package com.example.customer.dto;

import com.example.customer.entity.AmlStatus;
import com.example.customer.entity.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerRiskProfileResponse {

    private Long id;
    private Long customerId;
    private RiskLevel riskLevel;
    private boolean pep;
    private boolean sanctionChecked;
    private AmlStatus amlStatus;
    private LocalDate lastReviewDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
