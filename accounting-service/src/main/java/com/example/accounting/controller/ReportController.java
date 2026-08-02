package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.BranchLedgerResponse;
import com.example.accounting.service.GeneralLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final GeneralLedgerService generalLedgerService;

    // "Ledger by Branch" — posted activity across every GL account for a single branch.
    @GetMapping("/ledger-by-branch")
    public ResponseEntity<ApiResponse<BranchLedgerResponse>> getLedgerByBranch(
            @RequestParam Long branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.success(generalLedgerService.getBranchLedger(branchId, dateFrom, dateTo)));
    }
}
