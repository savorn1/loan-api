package com.example.payment.entity;

public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED,
    // A payment-wallet deposit's checkout link/QR sat unpaid past its own expiry
    // deadline — see PaymentTransactionServiceImpl.mapWalletStatus.
    EXPIRED
}
