package com.example.payment.service;

import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.dto.CollectorProductivityRow;
import com.example.payment.dto.LargeTransactionRow;
import com.example.payment.dto.ParSummaryResponse;
import com.example.payment.dto.ProvisioningSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {

    ParSummaryResponse getParSummary();

    List<CollectionTrendPointResponse> getCollectionTrend(int months);

    ProvisioningSummaryResponse getProvisioningSummary();

    List<LargeTransactionRow> getLargeTransactions(BigDecimal threshold, int months);

    List<CollectorProductivityRow> getCollectorProductivity(int months);
}
