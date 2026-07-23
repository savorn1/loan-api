package com.example.accounting.service;

import com.example.accounting.dto.TrialBalanceRowResponse;

import java.util.List;

public interface TrialBalanceService {

    List<TrialBalanceRowResponse> getTrialBalance(Long financialPeriodId);
}
