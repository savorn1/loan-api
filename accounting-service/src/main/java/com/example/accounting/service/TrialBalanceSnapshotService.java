package com.example.accounting.service;

import com.example.accounting.dto.TrialBalanceResponse;

import java.util.List;

public interface TrialBalanceSnapshotService {

    TrialBalanceResponse generate(Long financialPeriodId);

    TrialBalanceResponse getById(Long id);

    List<TrialBalanceResponse> getAll(Long financialPeriodId);
}
