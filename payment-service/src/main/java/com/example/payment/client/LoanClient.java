package com.example.payment.client;

import com.example.payment.dto.ApplyPaymentRequest;
import com.example.payment.dto.LoanResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "loan-service")
public interface LoanClient {

    @GetMapping("/api/loans/{id}")
    LoanResponse getById(@PathVariable Long id);

    @PutMapping("/api/loans/{id}/apply-payment")
    LoanResponse applyPayment(@PathVariable Long id, @RequestBody ApplyPaymentRequest request);
}
