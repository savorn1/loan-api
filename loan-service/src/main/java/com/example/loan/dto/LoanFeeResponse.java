package com.example.loan.dto;

import com.example.loan.entity.FeeStatus;
import com.example.loan.entity.LoanFeeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanFeeResponse {

    private Long id;
    private Long loanId;
    private LoanFeeType type;
    private BigDecimal amount;
    private LocalDate chargedDate;
    private String description;
    private FeeStatus status;
    private LocalDateTime waivedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
