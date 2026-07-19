package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.common.PageResponse;
import com.example.payment.dto.GenerateScheduleRequest;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created", paymentService.create(request)));
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> createSchedule(@Valid @RequestBody GenerateScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment schedule created", paymentService.createSchedule(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(paymentService.getAll(page, size, sortBy, sortOrder));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByLoan(loanId)));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment marked as paid", paymentService.markAsPaid(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted", null));
    }
}
