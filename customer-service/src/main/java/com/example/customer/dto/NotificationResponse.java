package com.example.customer.dto;

import lombok.Data;

// Minimal projection of notification-service's NotificationResponse — only
// what IdentityExpiryScheduler needs to log the outcome.
@Data
public class NotificationResponse {

    private Long id;
    private String status;
    private String errorMessage;
}
