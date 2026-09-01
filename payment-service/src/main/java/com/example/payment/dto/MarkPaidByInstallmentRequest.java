package com.example.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Called by loan-service after its own LoanPayment/LoanPaymentDetail allocation
// flow (POST /loans/{id}/payments) fully pays off an installment — that flow
// never goes through this service's own markAsPaid(), so without this call the
// matching Payment row here would silently stay PENDING/OVERDUE forever. See
// PaymentController.markPaidByInstallment / PaymentServiceImpl.markPaidByInstallment.
@Data
@NoArgsConstructor
public class MarkPaidByInstallmentRequest {

    private Long loanId;
    private Integer installmentNumber;
    private LocalDate paidAt;
}
