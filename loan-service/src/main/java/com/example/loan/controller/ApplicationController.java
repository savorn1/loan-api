package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplicationApprovalRequest;
import com.example.loan.dto.ApplicationDocumentRequest;
import com.example.loan.dto.ApplicationDocumentResponse;
import com.example.loan.dto.ApplicationNoteRequest;
import com.example.loan.dto.ApplicationNoteResponse;
import com.example.loan.dto.ApplicationRequest;
import com.example.loan.dto.ApplicationResponse;
import com.example.loan.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> create(@Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted", applicationService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ApplicationResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(applicationService.getAll(page, size, sortBy, sortOrder));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByCustomer(customerId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Application updated", applicationService.update(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/start-review")
    public ResponseEntity<ApiResponse<ApplicationResponse>> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application under review", applicationService.startReview(id)));
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdraw(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn", applicationService.withdraw(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Application deleted", null));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<ApplicationDocumentResponse>> addDocument(
            @PathVariable Long id, @Valid @RequestBody ApplicationDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document added", applicationService.addDocument(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/documents/{documentId}/verify")
    public ResponseEntity<ApiResponse<ApplicationDocumentResponse>> verifyDocument(
            @PathVariable Long id, @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success("Document verified", applicationService.verifyDocument(id, documentId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/documents/{documentId}/reject")
    public ResponseEntity<ApiResponse<ApplicationDocumentResponse>> rejectDocument(
            @PathVariable Long id, @PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.success("Document rejected", applicationService.rejectDocument(id, documentId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id, @PathVariable Long documentId) {
        applicationService.deleteDocument(id, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<ApplicationNoteResponse>> addNote(
            @PathVariable Long id, @Valid @RequestBody ApplicationNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added", applicationService.addNote(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approvals")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addApproval(
            @PathVariable Long id, @Valid @RequestBody ApplicationApprovalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Decision recorded", applicationService.addApproval(id, request)));
    }
}
