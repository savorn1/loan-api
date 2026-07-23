package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.InterestSchemeDetailRequest;
import com.example.loanproduct.dto.InterestSchemeDetailResponse;
import com.example.loanproduct.dto.InterestSchemeRequest;
import com.example.loanproduct.dto.InterestSchemeResponse;
import com.example.loanproduct.service.InterestSchemeDetailService;
import com.example.loanproduct.service.InterestSchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interest-schemes")
@RequiredArgsConstructor
public class InterestSchemeController {

    private final InterestSchemeService interestSchemeService;
    private final InterestSchemeDetailService interestSchemeDetailService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<InterestSchemeResponse>> create(@Valid @RequestBody InterestSchemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interest scheme created", interestSchemeService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterestSchemeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(interestSchemeService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterestSchemeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(interestSchemeService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterestSchemeResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody InterestSchemeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interest scheme updated", interestSchemeService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        interestSchemeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Interest scheme deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{interestSchemeId}/details")
    public ResponseEntity<ApiResponse<InterestSchemeDetailResponse>> createDetail(
            @PathVariable UUID interestSchemeId, @Valid @RequestBody InterestSchemeDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interest scheme detail added",
                        interestSchemeDetailService.create(interestSchemeId, request)));
    }

    @GetMapping("/{interestSchemeId}/details")
    public ResponseEntity<ApiResponse<List<InterestSchemeDetailResponse>>> getDetails(@PathVariable UUID interestSchemeId) {
        return ResponseEntity.ok(ApiResponse.success(interestSchemeDetailService.getByScheme(interestSchemeId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{interestSchemeId}/details/{detailId}")
    public ResponseEntity<ApiResponse<InterestSchemeDetailResponse>> updateDetail(
            @PathVariable UUID interestSchemeId, @PathVariable UUID detailId,
            @Valid @RequestBody InterestSchemeDetailRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interest scheme detail updated",
                interestSchemeDetailService.update(interestSchemeId, detailId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{interestSchemeId}/details/{detailId}")
    public ResponseEntity<ApiResponse<Void>> deleteDetail(
            @PathVariable UUID interestSchemeId, @PathVariable UUID detailId) {
        interestSchemeDetailService.delete(interestSchemeId, detailId);
        return ResponseEntity.ok(ApiResponse.success("Interest scheme detail deleted", null));
    }
}
