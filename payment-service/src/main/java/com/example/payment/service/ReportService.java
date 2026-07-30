package com.example.payment.service;

import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.dto.ParSummaryResponse;

import java.util.List;

public interface ReportService {

    ParSummaryResponse getParSummary();

    List<CollectionTrendPointResponse> getCollectionTrend(int months);
}
