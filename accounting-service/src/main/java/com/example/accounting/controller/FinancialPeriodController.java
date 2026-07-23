package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.FinancialPeriodRequest;
import com.example.accounting.dto.FinancialPeriodResponse;
import com.example.accounting.service.FinancialPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial-periods")
@RequiredArgsConstructor
public class FinancialPeriodController {

    private final FinancialPeriodService financialPeriodService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<FinancialPeriodResponse>> create(@Valid @RequestBody FinancialPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Financial period created", financialPeriodService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialPeriodResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(financialPeriodService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FinancialPeriodResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(financialPeriodService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FinancialPeriodResponse>> update(
            @PathVariable Long id, @Valid @RequestBody FinancialPeriodRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Financial period updated", financialPeriodService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<FinancialPeriodResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Financial period closed", financialPeriodService.close(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        financialPeriodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Financial period deleted", null));
    }
}
