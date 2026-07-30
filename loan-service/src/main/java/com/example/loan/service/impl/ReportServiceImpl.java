package com.example.loan.service.impl;

import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final LoanRepository loanRepository;

    @Override
    public PortfolioSummaryResponse getPortfolioSummary() {
        PortfolioSummaryResponse summary = loanRepository.aggregatePortfolioSummary();
        // sum(...) comes back null (not zero) when there are no ACTIVE loans —
        // normalize so callers never have to null-check the totals.
        if (summary.getTotalPrincipal() == null) summary.setTotalPrincipal(BigDecimal.ZERO);
        if (summary.getTotalOutstandingBalance() == null) summary.setTotalOutstandingBalance(BigDecimal.ZERO);
        return summary;
    }

    @Override
    public List<DisbursementTrendPointResponse> getDisbursementTrend(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        return loanRepository.aggregateDisbursementTrend(since);
    }

    @Override
    public List<LoanStatusBreakdownResponse> getStatusBreakdown() {
        return loanRepository.aggregateStatusBreakdown();
    }
}
