package com.example.loan.dto;

import com.example.loan.entity.CollateralStatus;
import com.example.loan.entity.CollateralType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanCollateralResponse {

    private Long id;
    private Long loanId;
    private CollateralType type;
    private String description;
    private BigDecimal estimatedValue;
    private String reference;
    private CollateralStatus status;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
