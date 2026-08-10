package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.BudgetLineRequest;
import com.example.accounting.dto.BudgetLineResponse;
import com.example.accounting.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetLineResponse>> create(@Valid @RequestBody BudgetLineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget line created", budgetService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetLineResponse>> update(@PathVariable Long id, @Valid @RequestBody BudgetLineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Budget line updated", budgetService.update(id, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetLineResponse>>> getByFinancialPeriod(
            @RequestParam Long financialPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getByFinancialPeriod(financialPeriodId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        budgetService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Budget line deleted", null));
    }
}
