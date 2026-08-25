package com.example.payment.dto;

import lombok.Data;

// payment-wallet's single-resource response envelope ({traceId, statusCode, message,
// data}) — used to unwrap PaymentWalletDepositResponse from PaymentWalletClient.
@Data
public class PaymentWalletApiEnvelope<T> {

    private String traceId;
    private int statusCode;
    private String message;
    private T data;
}
