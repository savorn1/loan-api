package com.example.payment.dto;

import com.example.payment.entity.PaymentMethodStatus;
import com.example.payment.entity.PaymentMethodType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentMethodResponse {

    private Long id;
    private String code;
    private String name;
    private PaymentMethodType type;
    private PaymentMethodStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
