package com.example.payment.client;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    // customer-service wraps every payload in ApiResponse ({traceId, statusCode,
    // message, data}) — the return type has to mirror that envelope so Feign
    // deserializes into `data` instead of silently leaving every field null
    // (see loan-service's CustomerClient for the bug this pattern avoids).
    @GetMapping("/api/customers/{id}")
    ApiResponse<CustomerResponse> getById(@PathVariable Long id);
}
