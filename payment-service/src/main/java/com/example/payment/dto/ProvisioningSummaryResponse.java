package com.example.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProvisioningSummaryResponse {

    private List<ProvisioningStageRow> stages;
    private BigDecimal totalOutstandingBalance;
    private BigDecimal totalProvisionAmount;
}
