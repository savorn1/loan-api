package com.example.accounting.client;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.LoanPortfolioSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "loan-service")
public interface LoanClient {

    // loan-service wraps every payload in ApiResponse ({traceId, statusCode, message,
    // data}) like every other service here — the return type has to mirror that envelope
    // so Feign deserializes into `data` instead of leaving every field null.
    @GetMapping("/api/loans/reports/portfolio-summary")
    ApiResponse<LoanPortfolioSummaryResponse> getPortfolioSummary();
}
