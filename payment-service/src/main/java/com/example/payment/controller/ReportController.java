package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.dto.CollectorProductivityRow;
import com.example.payment.dto.LargeTransactionRow;
import com.example.payment.dto.ParSummaryResponse;
import com.example.payment.dto.ProvisioningSummaryResponse;
import com.example.payment.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/par-summary")
    public ResponseEntity<ApiResponse<ParSummaryResponse>> getParSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getParSummary()));
    }

    @GetMapping("/collection-trend")
    public ResponseEntity<ApiResponse<List<CollectionTrendPointResponse>>> getCollectionTrend(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCollectionTrend(months)));
    }

    @GetMapping("/provisioning-summary")
    public ResponseEntity<ApiResponse<ProvisioningSummaryResponse>> getProvisioningSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getProvisioningSummary()));
    }

    @GetMapping("/large-transactions")
    public ResponseEntity<ApiResponse<List<LargeTransactionRow>>> getLargeTransactions(
            @RequestParam(defaultValue = "5000") BigDecimal threshold,
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getLargeTransactions(threshold, months)));
    }

    @GetMapping("/collector-productivity")
    public ResponseEntity<ApiResponse<List<CollectorProductivityRow>>> getCollectorProductivity(
            @RequestParam(defaultValue = "3") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCollectorProductivity(months)));
    }
}
