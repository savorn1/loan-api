package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.DateRangeLedgerResponse;
import com.example.accounting.dto.GeneralLedgerResponse;
import com.example.accounting.dto.GeneralLedgerSummaryRow;
import com.example.accounting.service.GeneralLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    // "General Ledger" overview report — every account's opening/period/closing balances for a period.
    @GetMapping("/ledger-summary")
    public ResponseEntity<ApiResponse<List<GeneralLedgerSummaryRow>>> getLedgerSummary(
            @RequestParam Long financialPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(generalLedgerService.getAllLedgers(financialPeriodId)));
    }

    // Backs both "Ledger by Date Range" (dateFrom/dateTo supplied) and "Account Transaction
    // History" (both omitted = full history) — see DateRangeLedgerResponse.
    @GetMapping("/{id}/ledger-by-date-range")
    public ResponseEntity<ApiResponse<DateRangeLedgerResponse>> getLedgerByDateRange(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.success(generalLedgerService.getLedgerForDateRange(id, dateFrom, dateTo)));
    }
}
