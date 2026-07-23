package com.example.loanproduct.controller;

import com.example.loanproduct.common.ApiResponse;
import com.example.loanproduct.dto.LoanProductDocumentRequest;
import com.example.loanproduct.dto.LoanProductDocumentResponse;
import com.example.loanproduct.service.LoanProductDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
public class LoanProductDocumentController {

    private final LoanProductDocumentService documentService;

    // Flat list across every product — GET /api/loan-products/documents.
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<LoanProductDocumentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(documentService.getAll()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{loanProductId}/documents")
    public ResponseEntity<ApiResponse<LoanProductDocumentResponse>> create(
            @PathVariable UUID loanProductId,
            @Valid @RequestBody LoanProductDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document added", documentService.create(loanProductId, request)));
    }

    @GetMapping("/{loanProductId}/documents")
    public ResponseEntity<ApiResponse<List<LoanProductDocumentResponse>>> getByProduct(@PathVariable UUID loanProductId) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getByLoanProduct(loanProductId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{loanProductId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<LoanProductDocumentResponse>> update(
            @PathVariable UUID loanProductId, @PathVariable Long documentId,
            @Valid @RequestBody LoanProductDocumentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Document updated", documentService.update(loanProductId, documentId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{loanProductId}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID loanProductId, @PathVariable Long documentId) {
        documentService.delete(loanProductId, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
