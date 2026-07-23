package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.LoanProductTermRequest;
import com.example.loanproduct.dto.LoanProductTermResponse;
import com.example.loanproduct.service.LoanProductTermService;
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
public class LoanProductTermController {

    private final LoanProductTermService termService;

    // Flat list across every product — GET /api/loan-products/terms.
    @GetMapping("/terms")
    public ResponseEntity<ApiResponse<List<LoanProductTermResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(termService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{loanProductId}/terms")
    public ResponseEntity<ApiResponse<LoanProductTermResponse>> create(
            @PathVariable UUID loanProductId,
            @Valid @RequestBody LoanProductTermRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Term added", termService.create(loanProductId, request)));
    }

    @GetMapping("/{loanProductId}/terms")
    public ResponseEntity<ApiResponse<List<LoanProductTermResponse>>> getByProduct(@PathVariable UUID loanProductId) {
        return ResponseEntity.ok(ApiResponse.success(termService.getByLoanProduct(loanProductId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/terms/{termId}")
    public ResponseEntity<ApiResponse<LoanProductTermResponse>> update(
            @PathVariable UUID loanProductId, @PathVariable Long termId,
            @Valid @RequestBody LoanProductTermRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Term updated", termService.update(loanProductId, termId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/terms/{termId}/set-default")
    public ResponseEntity<ApiResponse<LoanProductTermResponse>> setDefault(
            @PathVariable UUID loanProductId, @PathVariable Long termId) {
        return ResponseEntity.ok(ApiResponse.success("Default term updated", termService.setDefault(loanProductId, termId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{loanProductId}/terms/{termId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID loanProductId, @PathVariable Long termId) {
        termService.delete(loanProductId, termId);
        return ResponseEntity.ok(ApiResponse.success("Term deleted", null));
    }
}
