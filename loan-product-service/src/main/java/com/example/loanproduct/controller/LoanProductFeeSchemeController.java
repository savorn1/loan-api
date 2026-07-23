package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.LoanProductFeeSchemeRequest;
import com.example.loanproduct.dto.LoanProductFeeSchemeResponse;
import com.example.loanproduct.service.LoanProductFeeSchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
public class LoanProductFeeSchemeController {

    private final LoanProductFeeSchemeService feeSchemeMappingService;

    // Flat list across every product — GET /api/loan-products/fee-schemes.
    @GetMapping("/fee-schemes")
    public ResponseEntity<ApiResponse<List<LoanProductFeeSchemeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(feeSchemeMappingService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{loanProductId}/fee-schemes")
    public ResponseEntity<ApiResponse<LoanProductFeeSchemeResponse>> create(
            @PathVariable UUID loanProductId,
            @Valid @RequestBody LoanProductFeeSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee scheme added", feeSchemeMappingService.create(loanProductId, request)));
    }

    @GetMapping("/{loanProductId}/fee-schemes")
    public ResponseEntity<ApiResponse<List<LoanProductFeeSchemeResponse>>> getByProduct(@PathVariable UUID loanProductId) {
        return ResponseEntity.ok(ApiResponse.success(feeSchemeMappingService.getByLoanProduct(loanProductId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/fee-schemes/{mappingId}")
    public ResponseEntity<ApiResponse<LoanProductFeeSchemeResponse>> update(
            @PathVariable UUID loanProductId, @PathVariable UUID mappingId,
            @Valid @RequestBody LoanProductFeeSchemeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fee scheme updated",
                feeSchemeMappingService.update(loanProductId, mappingId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{loanProductId}/fee-schemes/{mappingId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID loanProductId, @PathVariable UUID mappingId) {
        feeSchemeMappingService.delete(loanProductId, mappingId);
        return ResponseEntity.ok(ApiResponse.success("Fee scheme removed", null));
    }
}
