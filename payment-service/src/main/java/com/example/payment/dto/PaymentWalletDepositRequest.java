package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// Request body for payment-wallet's merchant POST /deposits — see PaymentWalletClient.
@Data
@AllArgsConstructor
public class PaymentWalletDepositRequest {

    private BigDecimal amount;
    private String idempotencyKey;
}
