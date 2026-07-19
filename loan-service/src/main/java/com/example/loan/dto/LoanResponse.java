package com.example.loan.dto;

import com.example.loan.entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LoanStatus status;
    private String purpose;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime disbursedAt;
    private LocalDateTime closedAt;
    private LocalDate maturityDate;
    private BigDecimal monthlyInstallment;
    private BigDecimal outstandingBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
