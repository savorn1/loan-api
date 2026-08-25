package com.example.payment.service;

import com.example.payment.dto.PaymentTransactionRequest;
import com.example.payment.dto.PaymentTransactionResponse;
import com.example.payment.entity.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentTransactionService {

    PaymentTransactionResponse create(PaymentTransactionRequest request);

    PaymentTransactionResponse getById(Long id);

    List<PaymentTransactionResponse> getAll();

    PaymentTransactionResponse updateStatus(Long id, TransactionStatus status, String reason, String changedBy);

    // Records an already-completed loan repayment (markAsPaid, or a payment recorded
    // directly on a loan) as a SUCCESS transaction against the fixed internal
    // method/channel/gateway — see PaymentTransactionDefaultsSeeder. Unlike create(),
    // there's no PENDING step: the money has already moved by the time this is called.
    PaymentTransactionResponse createForLoanRepayment(Long customerId, Long loanId, BigDecimal amount);

    // Applies a signature-verified payment-wallet webhook delivery to the local
    // transaction matched by walletDepositId. No-op (rather than throwing) when the
    // status is unrecognized/non-terminal or already applied — see
    // PaymentWalletWebhookController, which is the only caller and has already
    // verified the delivery's HMAC signature before reaching here.
    void applyWalletDepositWebhook(Long walletDepositId, String walletStatus, String failureReason);
}
