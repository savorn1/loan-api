package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentGatewayRequest;
import com.example.payment.dto.PaymentGatewayResponse;
import com.example.payment.service.PaymentGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/gateways")
@RequiredArgsConstructor
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentGatewayResponse>> create(@Valid @RequestBody PaymentGatewayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment gateway created", paymentGatewayService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentGatewayResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentGatewayService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentGatewayResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(paymentGatewayService.getAll(page, size, sortBy, sortOrder, search));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentGatewayResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PaymentGatewayRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment gateway updated", paymentGatewayService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentGatewayService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Payment gateway deleted", null));
    }
}
