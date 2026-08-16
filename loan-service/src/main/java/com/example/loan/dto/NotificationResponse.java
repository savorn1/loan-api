package com.example.loan.dto;

import lombok.Data;

// Minimal projection of notification-service's NotificationResponse — only
// what LoanNotifier needs to log the outcome.
@Data
public class NotificationResponse {

    private Long id;
    private String status;
    private String errorMessage;
}
