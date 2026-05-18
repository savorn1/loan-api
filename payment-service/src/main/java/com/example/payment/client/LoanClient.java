package com.example.payment.client;

import com.example.payment.dto.LoanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "loan-service")
public interface LoanClient {

    @GetMapping("/api/loans/{id}")
    LoanResponse getById(@PathVariable Long id);
}
