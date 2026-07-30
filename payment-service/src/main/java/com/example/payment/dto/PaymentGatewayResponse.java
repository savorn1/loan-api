package com.example.payment.dto;

import com.example.payment.entity.PaymentGatewayStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentGatewayResponse {

    private Long id;
    private String code;
    private String name;
    private String provider;
    private String apiUrl;
    private PaymentGatewayStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
