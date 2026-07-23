package com.example.loan.dto;

import com.example.loan.entity.DisbursementMethod;
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
    private Long loanId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private DisbursementMethod method;
    private String reference;
    private List<LoanPaymentDetailResponse> allocations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
