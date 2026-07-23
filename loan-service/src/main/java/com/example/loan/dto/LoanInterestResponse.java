package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanInterestResponse {

    private Long id;
    private Long loanId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal rate;
    private BigDecimal amount;
    private LocalDateTime accruedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
