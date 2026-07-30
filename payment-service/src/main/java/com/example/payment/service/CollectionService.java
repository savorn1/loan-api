package com.example.payment.service;

import com.example.payment.dto.*;

import java.util.List;

public interface CollectionService {

    List<CollectionWorkqueueItemResponse> getWorkqueue(CollectionBucket bucket, Long assignedToUserId);

    CollectionCaseResponse getCase(Long loanId);

    CollectionCaseResponse assign(Long loanId, AssignCollectionCaseRequest request, String assignedBy);

    CollectionCaseResponse updateStatus(Long loanId, UpdateCollectionCaseStatusRequest request, String changedBy);

    List<CollectionStatusHistoryResponse> listStatusHistory(Long loanId);

    List<CollectionCaseAssignmentResponse> listAssignments(Long loanId);

    List<CollectionActivityResponse> listActivities(Long loanId);

    CollectionActivityResponse addActivity(Long loanId, CollectionActivityRequest request, String authorName);

    List<CollectionPromiseResponse> listPromises(Long loanId);

    CollectionPromiseResponse addPromise(Long loanId, CollectionPromiseRequest request, String authorName);

    CollectionPromiseResponse resolvePromise(Long loanId, Long promiseId, ResolveCollectionPromiseRequest request);

    List<CollectionLetterResponse> listLetters(Long loanId);

    CollectionLetterResponse addLetter(Long loanId, CollectionLetterRequest request, String authorName);

    CollectionLetterResponse updateLetterStatus(Long loanId, Long letterId, UpdateCollectionLetterStatusRequest request);
}
