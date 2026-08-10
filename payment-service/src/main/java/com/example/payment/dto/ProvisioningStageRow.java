package com.example.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProvisioningStageRow {

    // STAGE_1 (performing), STAGE_2 (DPD 1-90, underperforming), STAGE_3 (DPD 90+, credit-impaired) —
    // a simplified IFRS 9-style staging model, not a regulator-certified ECL calculation.
    private String stage;
    private String label;
    private long loanCount;
    private BigDecimal outstandingBalance;
    private BigDecimal provisionRatePercent;
    private BigDecimal provisionAmount;
}
