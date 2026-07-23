package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.GeneralLedgerResponse;
import com.example.accounting.service.GeneralLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gl-accounts")
@RequiredArgsConstructor
public class GeneralLedgerController {

    private final GeneralLedgerService generalLedgerService;

    @GetMapping("/{id}/ledger")
    public ResponseEntity<ApiResponse<GeneralLedgerResponse>> getLedger(
            @PathVariable Long id, @RequestParam Long financialPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(generalLedgerService.getLedger(id, financialPeriodId)));
    }
}
