package com.example.payment.client;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.ApplyPaymentRequest;
import com.example.payment.dto.LoanResponse;
import com.example.payment.dto.PortfolioSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "loan-service")
public interface LoanClient {

    // loan-service wraps every payload in ApiResponse ({traceId, statusCode, message,
    // data}) just like customer-service (see CustomerClient) — the return type has to
    // mirror that envelope so Feign deserializes into `data` instead of leaving every
    // field null. Previously returned a bare LoanResponse; every existing caller only
    // used this to trigger a 404 on a missing loan and discarded the (silently
    // all-null) result, which is how this went unnoticed until CollectionServiceImpl
    // became the first caller to actually read the fields back.
    @GetMapping("/api/loans/{id}")
    ApiResponse<LoanResponse> getById(@PathVariable Long id);

    @PutMapping("/api/loans/{id}/apply-payment")
    ApiResponse<LoanResponse> applyPayment(@PathVariable Long id, @RequestBody ApplyPaymentRequest request);

    @GetMapping("/api/loans/reports/portfolio-summary")
    ApiResponse<PortfolioSummaryResponse> getPortfolioSummary();
}
