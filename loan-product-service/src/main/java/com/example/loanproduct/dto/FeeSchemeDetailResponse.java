package com.example.loanproduct.dto;

import com.example.loanproduct.entity.FeeCalculationMethod;
import com.example.loanproduct.entity.FeeChargeTiming;
import com.example.loanproduct.entity.FeeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeeSchemeDetailResponse {

    private UUID id;
    private UUID feeSchemeId;
    private FeeType type;
    private FeeCalculationMethod calculationMethod;
    private BigDecimal amount;
    private FeeChargeTiming chargeTiming;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
