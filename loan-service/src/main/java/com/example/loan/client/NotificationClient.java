package com.example.loan.client;

import com.example.loan.common.ApiResponse;
import com.example.loan.dto.NotificationRequest;
import com.example.loan.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    // notification-service wraps every payload in ApiResponse, same as
    // customer-service/accounting-service — see CustomerClient's comment for
    // what happens when a Feign client's return type doesn't mirror that envelope.
    @PostMapping("/api/notifications")
    ApiResponse<NotificationResponse> create(@RequestBody NotificationRequest request);
}
