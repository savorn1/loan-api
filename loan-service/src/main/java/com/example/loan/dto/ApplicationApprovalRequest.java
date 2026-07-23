package com.example.loan.dto;

import com.example.loan.entity.ApprovalDecision;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicationApprovalRequest {

    @NotNull
    private ApprovalDecision decision;

    // Required when decision is APPROVED (validated in ApplicationServiceImpl, since their
    // presence depends on another field's value — not expressible with simple bean validation).
    @DecimalMin("1000.00")
    private BigDecimal approvedAmount;

    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private BigDecimal approvedInterestRate;

    @Min(1)
    @Max(360)
    private Integer approvedTermMonths;

    private String comments;
}
