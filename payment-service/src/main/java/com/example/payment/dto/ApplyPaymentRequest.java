package com.example.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ApplyPaymentRequest {

    private BigDecimal amount;

    // See loan-service's mirror of this DTO — controls whether applyPayment()
    // synthesizes another payment-transactions ledger row for this application.
    // Defaults true so PaymentServiceImpl.markAsPaid's existing single-arg
    // construction is unaffected; PaymentTransactionServiceImpl sets it false.
    private boolean recordTransaction = true;

    public ApplyPaymentRequest(BigDecimal amount) {
        this.amount = amount;
    }
}
