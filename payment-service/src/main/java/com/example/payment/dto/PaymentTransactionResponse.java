package com.example.payment.dto;

import com.example.payment.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentTransactionResponse {

    private Long id;
    private String paymentNo;
    private String referenceNo;
    private Long customerId;
    private String customerName;
    private Long paymentMethodId;
    private String paymentMethodName;
    // Lets the frontend tell an auto-booked internal transaction (see
    // PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE) apart from one that went
    // through a real gateway, without hardcoding the method's display name.
    private String paymentMethodCode;
    private Long paymentChannelId;
    private String paymentChannelName;
    private Long paymentGatewayId;
    private String paymentGatewayName;
    private String businessType;
    private String businessReference;
    private String currency;
    private BigDecimal amount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private TransactionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String refundedBy;
    private LocalDateTime refundedAt;
    private String refundReason;
    private List<PaymentTransactionItemResponse> items;
}
