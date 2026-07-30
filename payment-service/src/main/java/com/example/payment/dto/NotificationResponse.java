package com.example.payment.dto;

import lombok.Data;

// Minimal projection of notification-service's NotificationResponse — only
// what the schedulers need to log the outcome.
@Data
public class NotificationResponse {

    private Long id;
    private String status;
    private String errorMessage;
}
