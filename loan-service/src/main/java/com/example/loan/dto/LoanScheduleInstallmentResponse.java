package com.example.loan.dto;

import com.example.loan.entity.ScheduleInstallmentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanScheduleInstallmentResponse {

    private Long id;
    private Long scheduleId;
    private Long loanId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal outstandingBalance;
    private ScheduleInstallmentStatus status;
    private LocalDateTime createdAt;
}
