package com.example.payment.dto;

import com.example.payment.entity.PaymentChannelStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentChannelResponse {

    private Long id;
    private String code;
    private String name;
    private String channelType;
    private PaymentChannelStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
