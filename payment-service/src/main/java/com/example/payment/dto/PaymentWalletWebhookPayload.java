package com.example.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Mirrors payment-wallet's WebhookEvent delivery body — see
// PaymentWalletWebhookController. transactionId is the *wallet-side* deposit id
// (what this service stores as PaymentTransaction.walletDepositId), not our own id.
@Data
public class PaymentWalletWebhookPayload {

    private String eventType;
    private Long transactionId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String failureReason;
    private LocalDateTime occurredAt;
}
