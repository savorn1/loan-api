package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.LoansReceivableReconciliationResponse;
import com.example.accounting.dto.ReconciliationPostingResponse;
import com.example.accounting.dto.ReconciliationSnapshotResponse;
import com.example.accounting.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/loans-receivable")
    public ResponseEntity<ApiResponse<LoansReceivableReconciliationResponse>> loansReceivable() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.reconcileLoansReceivable()));
    }

    @GetMapping("/loans-receivable/history")
    public ResponseEntity<ApiResponse<List<ReconciliationSnapshotResponse>>> history() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getHistory()));
    }

    @GetMapping("/loans-receivable/postings")
    public ResponseEntity<ApiResponse<List<ReconciliationPostingResponse>>> postings() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getPostings()));
    }

    // Lets an admin force a check on demand (e.g. right after fixing something) instead of
    // waiting for ReconciliationScheduler's next run — same computation, just persisted.
    @PostMapping("/loans-receivable/snapshot")
    public ResponseEntity<ApiResponse<ReconciliationSnapshotResponse>> takeSnapshot() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.takeSnapshot()));
    }
}
