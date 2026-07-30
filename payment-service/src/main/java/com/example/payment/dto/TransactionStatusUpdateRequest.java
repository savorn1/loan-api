package com.example.payment.dto;

import com.example.payment.entity.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionStatusUpdateRequest {

    @NotNull
    private TransactionStatus status;
}
