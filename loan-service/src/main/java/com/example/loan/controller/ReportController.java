package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.dto.ApprovalFunnelResponse;
import com.example.loan.dto.ConcentrationRiskResponse;
import com.example.loan.dto.DataQualityExceptionRow;
import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;
import com.example.loan.dto.PricingSummaryResponse;
import com.example.loan.dto.StatusAuditEntryResponse;
import com.example.loan.dto.VintageCohortResponse;
import com.example.loan.entity.LoanStatus;
import com.example.loan.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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

    @GetMapping("/vintage-analysis")
    public ResponseEntity<ApiResponse<List<VintageCohortResponse>>> getVintageAnalysis(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getVintageAnalysis(months)));
    }

    @GetMapping("/concentration-risk")
    public ResponseEntity<ApiResponse<ConcentrationRiskResponse>> getConcentrationRisk(
            @RequestParam(defaultValue = "10") int topN) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getConcentrationRisk(topN)));
    }

    @GetMapping("/approval-funnel")
    public ResponseEntity<ApiResponse<ApprovalFunnelResponse>> getApprovalFunnel(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getApprovalFunnel(months)));
    }

    @GetMapping("/status-audit-trail")
    public ResponseEntity<ApiResponse<List<StatusAuditEntryResponse>>> getStatusAuditTrail(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getStatusAuditTrail(dateFrom, dateTo, status, limit)));
    }

    @GetMapping("/pricing-summary")
    public ResponseEntity<ApiResponse<PricingSummaryResponse>> getPricingSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPricingSummary()));
    }

    @GetMapping("/data-quality-exceptions")
    public ResponseEntity<ApiResponse<List<DataQualityExceptionRow>>> getDataQualityExceptions(
            @RequestParam(defaultValue = "30") int staleDays) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDataQualityExceptions(staleDays)));
    }
}
