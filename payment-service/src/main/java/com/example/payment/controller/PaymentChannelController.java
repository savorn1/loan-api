package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentChannelRequest;
import com.example.payment.dto.PaymentChannelResponse;
import com.example.payment.service.PaymentChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/channels")
@RequiredArgsConstructor
public class PaymentChannelController {

    private final PaymentChannelService paymentChannelService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentChannelResponse>> create(@Valid @RequestBody PaymentChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment channel created", paymentChannelService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentChannelResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentChannelService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentChannelResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(paymentChannelService.getAll(page, size, sortBy, sortOrder, search));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentChannelResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PaymentChannelRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment channel updated", paymentChannelService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentChannelService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Payment channel deleted", null));
    }
}
