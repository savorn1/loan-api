package com.example.accounting.service;

import com.example.accounting.dto.FinancialPeriodRequest;
import com.example.accounting.dto.FinancialPeriodResponse;

import java.util.List;

public interface FinancialPeriodService {

    FinancialPeriodResponse create(FinancialPeriodRequest request);

    FinancialPeriodResponse getById(Long id);

    List<FinancialPeriodResponse> getAll();

    FinancialPeriodResponse update(Long id, FinancialPeriodRequest request);

    FinancialPeriodResponse close(Long id);

    void delete(Long id);
}
