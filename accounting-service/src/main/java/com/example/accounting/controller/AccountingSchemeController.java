package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.AccountingSchemeRequest;
import com.example.accounting.dto.AccountingSchemeResponse;
import com.example.accounting.service.AccountingSchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting-schemes")
@RequiredArgsConstructor
public class AccountingSchemeController {

    private final AccountingSchemeService accountingSchemeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountingSchemeResponse>> create(@Valid @RequestBody AccountingSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Accounting scheme created", accountingSchemeService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountingSchemeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountingSchemeService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountingSchemeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(accountingSchemeService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountingSchemeResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AccountingSchemeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Accounting scheme updated", accountingSchemeService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountingSchemeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Accounting scheme deleted", null));
    }
}
