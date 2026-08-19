package com.example.payment.controller;

import com.example.payment.common.ApiResponse;
import com.example.payment.dto.*;
import com.example.payment.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollectionWorkqueueItemResponse>>> getWorkqueue(
            @RequestParam(required = false) CollectionBucket bucket,
            @RequestParam(required = false) Long assignedToUserId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getWorkqueue(bucket, assignedToUserId)));
    }

    // Same response shape as the plain workqueue above, but computed live from
    // installment due dates rather than depending on OverdueScheduler's nightly cron
    // having already flipped a payment's status to OVERDUE.
    @GetMapping("/live")
    public ResponseEntity<ApiResponse<List<CollectionWorkqueueItemResponse>>> getLiveOverdueLoans(
            @RequestParam(required = false) CollectionBucket bucket,
            @RequestParam(required = false) Long assignedToUserId) {
        return ResponseEntity.ok(
                ApiResponse.success(collectionService.getLiveOverdueLoans(bucket, assignedToUserId)));
    }

    @GetMapping("/{loanId}/case")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> getCase(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getCase(loanId)));
    }

    @PutMapping("/{loanId}/assign")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> assign(
            @PathVariable Long loanId, @Valid @RequestBody AssignCollectionCaseRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Collection case assigned", collectionService.assign(loanId, request, actorName(authentication))));
    }

    @PutMapping("/{loanId}/status")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> updateStatus(
            @PathVariable Long loanId, @Valid @RequestBody UpdateCollectionCaseStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Collection case status updated",
                collectionService.updateStatus(loanId, request, actorName(authentication))));
    }

    @GetMapping("/{loanId}/status-history")
    public ResponseEntity<ApiResponse<List<CollectionStatusHistoryResponse>>> listStatusHistory(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.listStatusHistory(loanId)));
    }

    @GetMapping("/{loanId}/assignments")
    public ResponseEntity<ApiResponse<List<CollectionCaseAssignmentResponse>>> listAssignments(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.listAssignments(loanId)));
    }

    @GetMapping("/{loanId}/activities")
    public ResponseEntity<ApiResponse<List<CollectionActivityResponse>>> listActivities(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.listActivities(loanId)));
    }

    @PostMapping("/{loanId}/activities")
    public ResponseEntity<ApiResponse<CollectionActivityResponse>> addActivity(
            @PathVariable Long loanId, @Valid @RequestBody CollectionActivityRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Activity logged", collectionService.addActivity(loanId, request, actorName(authentication))));
    }

    @GetMapping("/{loanId}/promises")
    public ResponseEntity<ApiResponse<List<CollectionPromiseResponse>>> listPromises(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.listPromises(loanId)));
    }

    @PostMapping("/{loanId}/promises")
    public ResponseEntity<ApiResponse<CollectionPromiseResponse>> addPromise(
            @PathVariable Long loanId, @Valid @RequestBody CollectionPromiseRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Promise to pay recorded", collectionService.addPromise(loanId, request, actorName(authentication))));
    }

    @PutMapping("/{loanId}/promises/{promiseId}/resolve")
    public ResponseEntity<ApiResponse<CollectionPromiseResponse>> resolvePromise(
            @PathVariable Long loanId, @PathVariable Long promiseId,
            @Valid @RequestBody ResolveCollectionPromiseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Promise updated", collectionService.resolvePromise(loanId, promiseId, request)));
    }

    @GetMapping("/{loanId}/letters")
    public ResponseEntity<ApiResponse<List<CollectionLetterResponse>>> listLetters(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.listLetters(loanId)));
    }

    @PostMapping("/{loanId}/letters")
    public ResponseEntity<ApiResponse<CollectionLetterResponse>> addLetter(
            @PathVariable Long loanId, @Valid @RequestBody CollectionLetterRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Letter recorded", collectionService.addLetter(loanId, request, actorName(authentication))));
    }

    @PutMapping("/{loanId}/letters/{letterId}/status")
    public ResponseEntity<ApiResponse<CollectionLetterResponse>> updateLetterStatus(
            @PathVariable Long loanId, @PathVariable Long letterId,
            @Valid @RequestBody UpdateCollectionLetterStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Letter status updated", collectionService.updateLetterStatus(loanId, letterId, request)));
    }

    private String actorName(Authentication authentication) {
        return authentication != null && authentication.getName() != null ? authentication.getName() : "system";
    }
}
