package com.example.notification.dto;

import com.example.notification.entity.NotificationChannel;
import com.example.notification.entity.NotificationStatus;
import com.example.notification.entity.RecipientType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class NotificationFilterRequest {

    private RecipientType recipientType;
    private Long recipientId;
    private NotificationChannel channel;
    private NotificationStatus status;

    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
