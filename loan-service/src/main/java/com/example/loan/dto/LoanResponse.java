package com.example.loan.dto;

import com.example.loan.entity.LoanStatus;
import com.example.loan.entity.TermUnit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanResponse {

    private Long id;
    private String loanNo;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private UUID loanProductId;
    private String loanProductName;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer termMonths;
    private TermUnit termUnit;
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
