package com.example.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerAuditLogResponse {

    private Long id;
    private Long customerId;
    private String action;
    private Long userId;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
