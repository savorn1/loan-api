package com.example.notification.service;

import com.example.notification.common.PageResponse;
import com.example.notification.dto.NotificationFilterRequest;
import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;

public interface NotificationService {

    NotificationResponse create(NotificationRequest request);

    NotificationResponse getById(Long id);

    PageResponse<NotificationResponse> list(NotificationFilterRequest filter);
}
