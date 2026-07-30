package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentMethodRequest;
import com.example.payment.dto.PaymentMethodResponse;
import com.example.payment.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> create(@Valid @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment method created", paymentMethodService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentMethodService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentMethodResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(paymentMethodService.getAll(page, size, sortBy, sortOrder, search));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment method updated", paymentMethodService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Payment method deleted", null));
    }
}
