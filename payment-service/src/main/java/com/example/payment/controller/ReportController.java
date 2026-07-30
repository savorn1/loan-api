package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.dto.ParSummaryResponse;
import com.example.payment.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
