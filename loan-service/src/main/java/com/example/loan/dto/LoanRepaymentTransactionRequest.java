package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Body for POST /api/payments/transactions/loan-repayment on payment-service — mirrors
// its own LoanRepaymentTransactionRequest. See PaymentClient.createLoanRepaymentTransaction.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentTransactionRequest {

    private Long customerId;
    private Long loanId;
    private BigDecimal amount;
}
