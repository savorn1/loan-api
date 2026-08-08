package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.LoansReceivableReconciliationResponse;
import com.example.accounting.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/loans-receivable")
    public ResponseEntity<ApiResponse<LoansReceivableReconciliationResponse>> loansReceivable() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.reconcileLoansReceivable()));
    }
}
