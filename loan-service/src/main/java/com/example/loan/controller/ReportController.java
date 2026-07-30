package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;
import com.example.loan.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/portfolio-summary")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>> getPortfolioSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPortfolioSummary()));
    }

    @GetMapping("/disbursement-trend")
    public ResponseEntity<ApiResponse<List<DisbursementTrendPointResponse>>> getDisbursementTrend(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDisbursementTrend(months)));
    }

    @GetMapping("/status-breakdown")
    public ResponseEntity<ApiResponse<List<LoanStatusBreakdownResponse>>> getStatusBreakdown() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getStatusBreakdown()));
    }
}
