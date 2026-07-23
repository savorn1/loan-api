package com.example.loan.dto;

import com.example.loan.entity.PenaltyStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanPenaltyResponse {

    private Long id;
    private Long loanId;
    private BigDecimal amount;
    private String reason;
    private LocalDate appliedDate;
    private PenaltyStatus status;
    private LocalDateTime waivedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
