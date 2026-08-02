package com.example.loan.client;

import com.example.loan.common.ApiResponse;
import com.example.loan.dto.LoanProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "loan-product-service")
public interface LoanProductClient {

    // loan-product-service wraps every payload in ApiResponse ({traceId, statusCode,
    // message, data}) — the return type has to mirror that envelope so Feign
    // deserializes into `data` instead of silently leaving every field null.
    @GetMapping("/api/loan-products/{id}")
    ApiResponse<LoanProductResponse> getById(@PathVariable UUID id);
}
