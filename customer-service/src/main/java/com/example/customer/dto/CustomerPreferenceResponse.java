package com.example.customer.dto;

import com.example.customer.entity.NotificationMethod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerPreferenceResponse {

    private Long id;
    private Long customerId;
    private String language;
    private String currency;
    private NotificationMethod notificationMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
