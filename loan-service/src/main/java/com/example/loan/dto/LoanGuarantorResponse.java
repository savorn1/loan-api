package com.example.loan.dto;

import com.example.loan.entity.GuarantorStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanGuarantorResponse {

    private Long id;
    private Long loanId;
    private String name;
    private String phone;
    private String relationship;
    private BigDecimal guaranteedAmount;
    private GuarantorStatus status;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
