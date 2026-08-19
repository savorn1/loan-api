package com.example.payment.dto;

import com.example.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentStatusHistoryResponse {

    private Long id;
    private Long paymentId;
    private PaymentStatus fromStatus;
    private PaymentStatus toStatus;
    private String changedBy;
    private String note;
    private LocalDateTime changedAt;
}
