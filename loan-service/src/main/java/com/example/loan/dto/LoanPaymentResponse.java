package com.example.loan.dto;

import com.example.loan.entity.DisbursementMethod;
import com.example.loan.entity.PaymentReversalStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanPaymentResponse {

    private Long id;
    private String paymentNo;
    private Long loanId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private DisbursementMethod method;
    private String reference;
    private List<LoanPaymentDetailResponse> allocations;
    private PaymentReversalStatus reversalStatus;
    private String reversalReason;
    private String reversalRequestedBy;
    private LocalDateTime reversalRequestedAt;
    private String reversalReviewedBy;
    private LocalDateTime reversalReviewedAt;
    private String reversalRejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
