package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.TermTemplateRequest;
import com.example.loanproduct.dto.TermTemplateResponse;
import com.example.loanproduct.service.TermTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/term-templates")
@RequiredArgsConstructor
public class TermTemplateController {

    private final TermTemplateService termTemplateService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TermTemplateResponse>> create(@Valid @RequestBody TermTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Term template created", termTemplateService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TermTemplateResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(termTemplateService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TermTemplateResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(termTemplateService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TermTemplateResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody TermTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Term template updated", termTemplateService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        termTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Term template deleted", null));
    }
}
