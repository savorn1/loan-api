package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.TrialBalanceResponse;
import com.example.accounting.service.TrialBalanceSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trial-balance/snapshots")
@RequiredArgsConstructor
public class TrialBalanceSnapshotController {

    private final TrialBalanceSnapshotService trialBalanceSnapshotService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TrialBalanceResponse>> generate(@RequestParam Long financialPeriodId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trial balance snapshot generated", trialBalanceSnapshotService.generate(financialPeriodId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrialBalanceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trialBalanceSnapshotService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrialBalanceResponse>>> getAll(@RequestParam Long financialPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(trialBalanceSnapshotService.getAll(financialPeriodId)));
    }
}
