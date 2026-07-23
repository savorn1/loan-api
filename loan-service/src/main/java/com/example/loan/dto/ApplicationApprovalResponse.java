package com.example.loan.dto;

import com.example.loan.entity.ApprovalDecision;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationApprovalResponse {

    private Long id;
    private Long applicationId;
    private String approverName;
    private ApprovalDecision decision;
    private BigDecimal approvedAmount;
    private BigDecimal approvedInterestRate;
    private Integer approvedTermMonths;
    private String comments;
    private LocalDateTime decidedAt;
}
