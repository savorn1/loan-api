package com.example.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Body for loan-service's inter-service call into POST /api/payments/transactions/loan-repayment
// — see PaymentTransactionController and LoanClient's loan-service-side counterpart of this DTO.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentTransactionRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Long loanId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
