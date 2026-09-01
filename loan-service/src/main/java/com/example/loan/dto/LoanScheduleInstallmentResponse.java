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
    // Sum of non-reversed LoanPaymentDetail.principalAmount+interestAmount rows applied
    // to this installment — see LoanServiceImpl.allocatePayment for the same computation.
    // Not the same as (totalAmount - outstandingBalance): outstandingBalance here is
    // frozen at schedule-generation time and never updated on payment.
    private BigDecimal paidAmount;
    private ScheduleInstallmentStatus status;
    private LocalDateTime createdAt;
}
