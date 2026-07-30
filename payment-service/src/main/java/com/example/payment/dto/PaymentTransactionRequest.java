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

    @Size(max = 100)
    private String businessReference;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Size(max = 100)
    private String referenceNo;
}
