package com.example.loan.controller;

import com.example.loan.common.ApiResponse;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.GroupLoanApplicationApprovalRequest;
import com.example.loan.dto.GroupLoanApplicationDocumentRequest;
import com.example.loan.dto.GroupLoanApplicationDocumentResponse;
import com.example.loan.dto.GroupLoanApplicationRequest;
import com.example.loan.dto.GroupLoanApplicationResponse;
import com.example.loan.service.GroupLoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-loan-applications")
@RequiredArgsConstructor
public class GroupLoanApplicationController {

    private final GroupLoanApplicationService groupLoanApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupLoanApplicationResponse>> create(
            @Valid @RequestBody GroupLoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted", groupLoanApplicationService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupLoanApplicationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(groupLoanApplicationService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<GroupLoanApplicationResponse>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(groupLoanApplicationService.getAll(page, size, sortBy, sortOrder));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<GroupLoanApplicationResponse>>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupLoanApplicationService.getByGroup(groupId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/start-review")
    public ResponseEntity<ApiResponse<GroupLoanApplicationResponse>> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Application under review", groupLoanApplicationService.startReview(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<GroupLoanApplicationResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application cancelled", groupLoanApplicationService.cancel(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approvals")
    public ResponseEntity<ApiResponse<GroupLoanApplicationResponse>> addApproval(
            @PathVariable Long id, @Valid @RequestBody GroupLoanApplicationApprovalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Decision recorded", groupLoanApplicationService.addApproval(id, request)));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<GroupLoanApplicationDocumentResponse>> addDocument(
            @PathVariable Long id, @Valid @RequestBody GroupLoanApplicationDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document added", groupLoanApplicationService.addDocument(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/documents/{documentId}/verify")
    public ResponseEntity<ApiResponse<GroupLoanApplicationDocumentResponse>> verifyDocument(
            @PathVariable Long id, @PathVariable Long documentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Document verified", groupLoanApplicationService.verifyDocument(id, documentId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/documents/{documentId}/reject")
    public ResponseEntity<ApiResponse<GroupLoanApplicationDocumentResponse>> rejectDocument(
            @PathVariable Long id, @PathVariable Long documentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Document rejected", groupLoanApplicationService.rejectDocument(id, documentId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id, @PathVariable Long documentId) {
        groupLoanApplicationService.deleteDocument(id, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
