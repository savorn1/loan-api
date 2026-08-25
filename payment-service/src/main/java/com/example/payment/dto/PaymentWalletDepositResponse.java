package com.example.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Mirrors payment-wallet's merchant DepositResponse — only the fields this service
// actually reads; extra fields on the real payload are ignored by Jackson.
@Data
public class PaymentWalletDepositResponse {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String provider;
    private String providerReference;
    private String paymentUrl;
    private String qrCodeData;
    private String failureReason;
    private Long walletTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
