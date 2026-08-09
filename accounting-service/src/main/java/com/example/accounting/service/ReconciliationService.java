package com.example.accounting.service;

import com.example.accounting.dto.LoansReceivableReconciliationResponse;
import com.example.accounting.dto.ReconciliationPostingResponse;
import com.example.accounting.dto.ReconciliationSnapshotResponse;

import java.util.List;

public interface ReconciliationService {

    LoansReceivableReconciliationResponse reconcileLoansReceivable();

    // Computes the same check as reconcileLoansReceivable() and persists the result — called
    // by ReconciliationScheduler, not the read-only GET endpoint, so viewing the report
    // doesn't itself generate history noise.
    ReconciliationSnapshotResponse takeSnapshot();

    List<ReconciliationSnapshotResponse> getHistory();

    List<ReconciliationPostingResponse> getPostings();
}
