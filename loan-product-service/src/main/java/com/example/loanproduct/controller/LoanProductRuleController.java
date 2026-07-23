package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.LoanProductRuleRequest;
import com.example.loanproduct.dto.LoanProductRuleResponse;
import com.example.loanproduct.service.LoanProductRuleService;
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
public class LoanProductRuleController {

    private final LoanProductRuleService ruleService;

    // Flat list across every product — GET /api/loan-products/rules.
    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<List<LoanProductRuleResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(ruleService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{loanProductId}/rules")
    public ResponseEntity<ApiResponse<LoanProductRuleResponse>> create(
            @PathVariable UUID loanProductId,
            @Valid @RequestBody LoanProductRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rule added", ruleService.create(loanProductId, request)));
    }

    @GetMapping("/{loanProductId}/rules")
    public ResponseEntity<ApiResponse<List<LoanProductRuleResponse>>> getByProduct(@PathVariable UUID loanProductId) {
        return ResponseEntity.ok(ApiResponse.success(ruleService.getByLoanProduct(loanProductId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/rules/{ruleId}")
    public ResponseEntity<ApiResponse<LoanProductRuleResponse>> update(
            @PathVariable UUID loanProductId, @PathVariable Long ruleId,
            @Valid @RequestBody LoanProductRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Rule updated", ruleService.update(loanProductId, ruleId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{loanProductId}/rules/{ruleId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID loanProductId, @PathVariable Long ruleId) {
        ruleService.delete(loanProductId, ruleId);
        return ResponseEntity.ok(ApiResponse.success("Rule deleted", null));
    }
}
