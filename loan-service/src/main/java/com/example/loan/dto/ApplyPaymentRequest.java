package com.example.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ApplyPaymentRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    // Whether applyPayment() should synthesize a payment-transactions ledger row via
    // paymentClient.createLoanRepaymentTransaction (see LoanServiceImpl.applyPayment).
    // Defaults true for existing callers (PaymentServiceImpl.markAsPaid, the loan's own
    // "Apply payment to balance" action), which have no PaymentTransaction of their own
    // yet. payment-service's gateway-origin PaymentTransactionServiceImpl passes false —
    // its own transaction row already exists, so recording another here would
    // double-book the same repayment in the ledger.
    private boolean recordTransaction = true;

    public ApplyPaymentRequest(BigDecimal amount) {
        this.amount = amount;
    }
}
