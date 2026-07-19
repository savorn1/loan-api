package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> create(@Valid @RequestBody LoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created", loanService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<LoanResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(loanService.getAll(page, size, sortBy, sortOrder));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getByCustomer(customerId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LoanResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Loan approved", loanService.approve(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LoanResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Loan rejected", loanService.reject(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/disburse")
    public ResponseEntity<ApiResponse<LoanResponse>> disburse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Loan disbursed", loanService.disburse(id)));
    }

    @PutMapping("/{id}/apply-payment")
    public ResponseEntity<ApiResponse<LoanResponse>> applyPayment(@PathVariable Long id,
                                                                    @Valid @RequestBody ApplyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment applied", loanService.applyPayment(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        loanService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Loan deleted", null));
    }
}
