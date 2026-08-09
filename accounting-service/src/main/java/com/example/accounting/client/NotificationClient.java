package com.example.accounting.client;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.NotificationRequest;
import com.example.accounting.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    ApiResponse<NotificationResponse> create(@RequestBody NotificationRequest request);
}
