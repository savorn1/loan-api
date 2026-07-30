package com.example.notification.dto;

import com.example.notification.entity.NotificationChannel;
import com.example.notification.entity.NotificationStatus;
import com.example.notification.entity.RecipientType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private RecipientType recipientType;
    private Long recipientId;
    private NotificationChannel channel;
    private String recipientContact;
    private String subject;
    private String message;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
