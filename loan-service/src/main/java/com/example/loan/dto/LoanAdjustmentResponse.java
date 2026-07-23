package com.example.loan.dto;

import com.example.loan.entity.AdjustmentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanAdjustmentResponse {

    private Long id;
    private Long loanId;
    private AdjustmentType type;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
