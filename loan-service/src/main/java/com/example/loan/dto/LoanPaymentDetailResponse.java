package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// JSON field names (principalAllocated/interestAllocated/penaltyAllocated,
// scheduleDetailId) mirror the frontend's LoanPaymentAllocationResponse
// contract; the backing table is loan_payment_details.
@Data
@Builder
public class LoanPaymentDetailResponse {

    private Long id;
    private Long paymentId;
    private Long scheduleDetailId;
    private Integer installmentNumber;
    private BigDecimal principalAllocated;
    private BigDecimal interestAllocated;
    private BigDecimal penaltyAllocated;
    private LocalDateTime createdAt;
}
