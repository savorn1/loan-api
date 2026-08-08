package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.JournalAuditLogResponse;
import com.example.accounting.dto.JournalEntryGenerateRequest;
import com.example.accounting.dto.JournalEntryRequest;
import com.example.accounting.dto.JournalEntryResponse;
import com.example.accounting.service.JournalAuditLogService;
import com.example.accounting.service.JournalEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal-entries")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;
    private final JournalAuditLogService journalAuditLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryResponse>> create(@Valid @RequestBody JournalEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Journal entry created as draft", journalEntryService.create(request)));
    }

    // Called by loan-service/payment-service instead of them building lines themselves —
    // resolves transactionType -> JournalTemplate -> AccountingScheme and posts immediately.
    // No @PreAuthorize: this is an internal Feign call with no user JWT attached, unlike
    // the user-driven /{id}/post below.
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> generate(
            @Valid @RequestBody JournalEntryGenerateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Journal entry generated and posted", journalEntryService.generate(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JournalEntryResponse>>> getAll(
            @RequestParam(required = false) Long financialPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.getAll(financialPeriodId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> post(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Journal entry posted", journalEntryService.post(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> reverse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Journal entry reversed", journalEntryService.reverse(id)));
    }

    // Same operation as /{id}/reverse, without @PreAuthorize — for internal Feign calls (no
    // user JWT) triggered by an upstream correction, e.g. loan-service's voidDisbursement.
    @PostMapping("/{id}/system-reverse")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> systemReverse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Journal entry reversed", journalEntryService.reverse(id)));
    }

    @GetMapping("/{id}/audit-logs")
    public ResponseEntity<ApiResponse<List<JournalAuditLogResponse>>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(journalAuditLogService.getForEntry(id)));
    }
}
