package com.example.payment.dto;

import com.example.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;
    private Long loanId;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidAt;
    private PaymentStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
