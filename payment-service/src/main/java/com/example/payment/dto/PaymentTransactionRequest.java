package com.example.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentTransactionRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private Long paymentMethodId;

    @NotNull
    private Long paymentChannelId;

    @NotNull
    private Long paymentGatewayId;

    @NotBlank
    @Size(max = 50)
    private String businessType;

    // What this transaction settles — e.g. a loan id when businessType is
    // LOAN_PAYMENT. Required so every transaction actually stores a real, checkable
    // link (see PaymentTransactionServiceImpl.create) rather than an arbitrary string.
    @NotBlank
    @Size(max = 100)
    private String businessReference;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    // Optional breakdown of `amount` — how much of this transaction is principal vs.
    // interest. Not validated against `amount` (metadata, not an enforced split).
    @DecimalMin(value = "0.00")
    private BigDecimal principalAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal interestAmount;

    @Size(max = 100)
    private String referenceNo;
}
