package com.example.accounting.controller;

import com.example.accounting.common.ApiResponse;
import com.example.accounting.dto.GlAccountRequest;
import com.example.accounting.dto.GlAccountResponse;
import com.example.accounting.service.GlAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gl-accounts")
@RequiredArgsConstructor
public class GlAccountController {

    private final GlAccountService glAccountService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<GlAccountResponse>> create(@Valid @RequestBody GlAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("GL account created", glAccountService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GlAccountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(glAccountService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GlAccountResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(glAccountService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GlAccountResponse>> update(
            @PathVariable Long id, @Valid @RequestBody GlAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("GL account updated", glAccountService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        glAccountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("GL account deleted", null));
    }
}
