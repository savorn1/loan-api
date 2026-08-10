package com.example.loan.service;

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

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    PortfolioSummaryResponse getPortfolioSummary();

    List<DisbursementTrendPointResponse> getDisbursementTrend(int months);

    List<LoanStatusBreakdownResponse> getStatusBreakdown();

    List<VintageCohortResponse> getVintageAnalysis(int months);

    ConcentrationRiskResponse getConcentrationRisk(int topN);

    ApprovalFunnelResponse getApprovalFunnel(int months);

    List<StatusAuditEntryResponse> getStatusAuditTrail(LocalDate dateFrom, LocalDate dateTo, LoanStatus status, int limit);

    PricingSummaryResponse getPricingSummary();

    List<DataQualityExceptionRow> getDataQualityExceptions(int staleDays);
}
