package com.example.loanproduct.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InterestSchemeDetailResponse {

    private UUID id;
    private UUID interestSchemeId;
    private Integer minTerm;
    private Integer maxTerm;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
