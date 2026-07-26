package com.example.notification.dto;

import com.example.notification.entity.NotificationChannel;
import com.example.notification.entity.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotNull
    private RecipientType recipientType;

    @NotNull
    private Long recipientId;

    @NotNull
    private NotificationChannel channel;

    private String subject;

    @NotBlank
    private String message;
}
