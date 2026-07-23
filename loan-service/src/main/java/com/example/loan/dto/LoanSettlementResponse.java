package com.example.loan.dto;

import com.example.loan.entity.SettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanSettlementResponse {

    private Long id;
    private Long loanId;
    private BigDecimal settlementAmount;
    private LocalDate settlementDate;
    private SettlementStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
