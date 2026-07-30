package com.example.loan.service;

import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;

import java.util.List;

public interface ReportService {

    PortfolioSummaryResponse getPortfolioSummary();

    List<DisbursementTrendPointResponse> getDisbursementTrend(int months);

    List<LoanStatusBreakdownResponse> getStatusBreakdown();
}
