package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Body for PUT /api/payments/mark-paid-by-installment on payment-service — mirrors
// its own MarkPaidByInstallmentRequest. See PaymentClient.markPaidByInstallment.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkPaidByInstallmentRequest {

    private Long loanId;
    private Integer installmentNumber;
    private LocalDate paidAt;
}
