package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.FeeSchemeDetailRequest;
import com.example.loanproduct.dto.FeeSchemeDetailResponse;
import com.example.loanproduct.dto.FeeSchemeRequest;
import com.example.loanproduct.dto.FeeSchemeResponse;
import com.example.loanproduct.service.FeeSchemeDetailService;
import com.example.loanproduct.service.FeeSchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fee-schemes")
@RequiredArgsConstructor
public class FeeSchemeController {

    private final FeeSchemeService feeSchemeService;
    private final FeeSchemeDetailService feeSchemeDetailService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<FeeSchemeResponse>> create(@Valid @RequestBody FeeSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee scheme created", feeSchemeService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeSchemeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(feeSchemeService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeeSchemeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(feeSchemeService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeSchemeResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody FeeSchemeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fee scheme updated", feeSchemeService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        feeSchemeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Fee scheme deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{feeSchemeId}/details")
    public ResponseEntity<ApiResponse<FeeSchemeDetailResponse>> createDetail(
            @PathVariable UUID feeSchemeId, @Valid @RequestBody FeeSchemeDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fee scheme detail added", feeSchemeDetailService.create(feeSchemeId, request)));
    }

    @GetMapping("/{feeSchemeId}/details")
    public ResponseEntity<ApiResponse<List<FeeSchemeDetailResponse>>> getDetails(@PathVariable UUID feeSchemeId) {
        return ResponseEntity.ok(ApiResponse.success(feeSchemeDetailService.getByScheme(feeSchemeId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{feeSchemeId}/details/{detailId}")
    public ResponseEntity<ApiResponse<FeeSchemeDetailResponse>> updateDetail(
            @PathVariable UUID feeSchemeId, @PathVariable UUID detailId,
            @Valid @RequestBody FeeSchemeDetailRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Fee scheme detail updated",
                feeSchemeDetailService.update(feeSchemeId, detailId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{feeSchemeId}/details/{detailId}")
    public ResponseEntity<ApiResponse<Void>> deleteDetail(
            @PathVariable UUID feeSchemeId, @PathVariable UUID detailId) {
        feeSchemeDetailService.delete(feeSchemeId, detailId);
        return ResponseEntity.ok(ApiResponse.success("Fee scheme detail deleted", null));
    }
}
