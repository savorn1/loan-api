package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.LoanProductInterestSchemeRequest;
import com.example.loanproduct.dto.LoanProductInterestSchemeResponse;
import com.example.loanproduct.service.LoanProductInterestSchemeService;
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
public class LoanProductInterestSchemeController {

    private final LoanProductInterestSchemeService interestSchemeMappingService;

    // Flat list across every product — GET /api/loan-products/interest-schemes.
    @GetMapping("/interest-schemes")
    public ResponseEntity<ApiResponse<List<LoanProductInterestSchemeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(interestSchemeMappingService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{loanProductId}/interest-schemes")
    public ResponseEntity<ApiResponse<LoanProductInterestSchemeResponse>> create(
            @PathVariable UUID loanProductId,
            @Valid @RequestBody LoanProductInterestSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interest scheme added", interestSchemeMappingService.create(loanProductId, request)));
    }

    @GetMapping("/{loanProductId}/interest-schemes")
    public ResponseEntity<ApiResponse<List<LoanProductInterestSchemeResponse>>> getByProduct(@PathVariable UUID loanProductId) {
        return ResponseEntity.ok(ApiResponse.success(interestSchemeMappingService.getByLoanProduct(loanProductId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/interest-schemes/{mappingId}")
    public ResponseEntity<ApiResponse<LoanProductInterestSchemeResponse>> update(
            @PathVariable UUID loanProductId, @PathVariable UUID mappingId,
            @Valid @RequestBody LoanProductInterestSchemeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interest scheme updated",
                interestSchemeMappingService.update(loanProductId, mappingId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/interest-schemes/{mappingId}/set-default")
    public ResponseEntity<ApiResponse<LoanProductInterestSchemeResponse>> setDefault(
            @PathVariable UUID loanProductId, @PathVariable UUID mappingId) {
        return ResponseEntity.ok(ApiResponse.success("Default interest scheme updated",
                interestSchemeMappingService.setDefault(loanProductId, mappingId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{loanProductId}/interest-schemes/{mappingId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID loanProductId, @PathVariable UUID mappingId) {
        interestSchemeMappingService.delete(loanProductId, mappingId);
        return ResponseEntity.ok(ApiResponse.success("Interest scheme removed", null));
    }
}
