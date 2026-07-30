package com.example.payment.client;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.NotificationRequest;
import com.example.payment.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    // notification-service wraps every payload in ApiResponse, same as
    // loan-service/customer-service — see LoanClient's comment for what
    // happens when a Feign client's return type doesn't mirror that envelope.
    @PostMapping("/api/notifications")
    ApiResponse<NotificationResponse> create(@RequestBody NotificationRequest request);
}
