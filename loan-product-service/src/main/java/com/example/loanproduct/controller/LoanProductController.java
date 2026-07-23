package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.LoanProductRequest;
import com.example.loanproduct.dto.LoanProductResponse;
import com.example.loanproduct.service.LoanProductService;
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
public class LoanProductController {

    private final LoanProductService loanProductService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<LoanProductResponse>> create(@Valid @RequestBody LoanProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan product created", loanProductService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanProductResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(loanProductService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanProductResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(loanProductService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanProductResponse>> update(@PathVariable UUID id,
                                                                     @Valid @RequestBody LoanProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Loan product updated", loanProductService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        loanProductService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Loan product deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<LoanProductResponse>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Loan product published", loanProductService.publish(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/new-version")
    public ResponseEntity<ApiResponse<LoanProductResponse>> newVersion(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("New draft version created", loanProductService.newVersion(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/retire")
    public ResponseEntity<ApiResponse<LoanProductResponse>> retire(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Loan product retired", loanProductService.retire(id)));
    }
}
