package com.example.customer.client;

import com.example.customer.common.ApiResponse;
import com.example.customer.dto.NotificationRequest;
import com.example.customer.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    // notification-service wraps every payload in ApiResponse — the return type
    // has to mirror that envelope so Feign deserializes into `data` instead of
    // silently leaving every field null (see loan-service's NotificationClient
    // for the sibling of this client and the bug this pattern avoids).
    @PostMapping("/api/notifications")
    ApiResponse<NotificationResponse> create(@RequestBody NotificationRequest request);
}
