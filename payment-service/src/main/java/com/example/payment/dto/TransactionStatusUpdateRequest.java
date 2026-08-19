package com.example.payment.dto;

import com.example.payment.entity.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionStatusUpdateRequest {

    @NotNull
    private TransactionStatus status;

    // Required when status == REFUNDED — enforced in PaymentTransactionServiceImpl
    // rather than here, since the requirement is conditional on another field.
    private String reason;
}
