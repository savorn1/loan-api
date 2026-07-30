package com.example.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentTransactionItemResponse {

    private Long id;
    private Long paymentTransactionId;
    private String referenceType;
    private Long referenceId;
    private BigDecimal amount;
}
