package com.example.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mirrors notification-service's own NotificationRequest — see that service's
// dto package for the full contract this is sent against.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private RecipientType recipientType;
    private Long recipientId;
    private NotificationChannel channel;
    private String recipientContact;
    private String subject;
    private String message;
}
