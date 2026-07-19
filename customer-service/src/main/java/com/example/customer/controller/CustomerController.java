package com.example.customer.controller;

import com.example.customer.annotation.RateLimit;
import com.example.customer.common.ApiResponse;
import com.example.customer.common.PageResponse;
import com.example.customer.dto.CustomerRequest;
import com.example.customer.dto.CustomerResponse;
import com.example.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Only endpoint in this service actually wired to @RateLimit/RateLimitAspect — the
    // annotation and its Redis-backed aspect existed but were never applied to anything,
    // so the rate limiter was dead code. Customer creation is the natural candidate: it's
    // unauthenticated-adjacent (any authenticated user, not just admins) and writes to the DB.
    @RateLimit(max = 20, window = 60)
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created", customerService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(customerService.getAll(page, size, sortBy, sortOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable Long id,
                                                                  @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted", null));
    }
}
