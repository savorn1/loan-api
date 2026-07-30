package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.PaymentTransactionRequest;
import com.example.payment.dto.PaymentTransactionResponse;
import com.example.payment.dto.TransactionStatusUpdateRequest;
import com.example.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> create(
            @Valid @RequestBody PaymentTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created", paymentTransactionService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentTransactionService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(paymentTransactionService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody TransactionStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction status updated", paymentTransactionService.updateStatus(id, request.getStatus())));
    }
}
