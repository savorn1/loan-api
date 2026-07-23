package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.DocumentTemplateRequest;
import com.example.loanproduct.dto.DocumentTemplateResponse;
import com.example.loanproduct.service.DocumentTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/document-templates")
@RequiredArgsConstructor
public class DocumentTemplateController {

    private final DocumentTemplateService documentTemplateService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentTemplateResponse>> create(@Valid @RequestBody DocumentTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document template created", documentTemplateService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentTemplateResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentTemplateService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentTemplateResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(documentTemplateService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentTemplateResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody DocumentTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Document template updated", documentTemplateService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        documentTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Document template deleted", null));
    }
}
