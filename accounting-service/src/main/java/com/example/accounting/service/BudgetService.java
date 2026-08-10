package com.example.accounting.service;

import com.example.accounting.dto.BudgetLineRequest;
import com.example.accounting.dto.BudgetLineResponse;

import java.util.List;

public interface BudgetService {

    BudgetLineResponse create(BudgetLineRequest request);

    BudgetLineResponse update(Long id, BudgetLineRequest request);

    List<BudgetLineResponse> getByFinancialPeriod(Long financialPeriodId);

    void delete(Long id);
}
