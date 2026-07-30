package com.example.payment.service.impl;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.dto.*;
import com.example.payment.entity.CollectionActivity;
import com.example.payment.entity.CollectionCase;
import com.example.payment.entity.CollectionCaseAssignment;
import com.example.payment.entity.CollectionLetter;
import com.example.payment.entity.CollectionPromise;
import com.example.payment.entity.CollectionStatusHistory;
import com.example.payment.entity.LetterStatus;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.entity.PromiseStatus;
import com.example.payment.repository.CollectionActivityRepository;
import com.example.payment.repository.CollectionCaseAssignmentRepository;
import com.example.payment.repository.CollectionCaseRepository;
import com.example.payment.repository.CollectionLetterRepository;
import com.example.payment.repository.CollectionPromiseRepository;
import com.example.payment.repository.CollectionStatusHistoryRepository;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.CollectionService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final PaymentRepository paymentRepository;
    private final CollectionCaseRepository collectionCaseRepository;
    private final CollectionActivityRepository collectionActivityRepository;
    private final CollectionStatusHistoryRepository collectionStatusHistoryRepository;
    private final CollectionCaseAssignmentRepository collectionCaseAssignmentRepository;
    private final CollectionPromiseRepository collectionPromiseRepository;
    private final CollectionLetterRepository collectionLetterRepository;
    private final LoanClient loanClient;
    private final CustomerClient customerClient;

    @Override
    public List<CollectionWorkqueueItemResponse> getWorkqueue(CollectionBucket bucket, Long assignedToUserId) {
        Map<Long, List<Payment>> overdueByLoan = paymentRepository.findByStatus(PaymentStatus.OVERDUE).stream()
                .collect(Collectors.groupingBy(Payment::getLoanId));

        LocalDate today = LocalDate.now();
        List<CollectionWorkqueueItemResponse> items = new ArrayList<>();

        for (Map.Entry<Long, List<Payment>> entry : overdueByLoan.entrySet()) {
            Long loanId = entry.getKey();
            List<Payment> overduePayments = entry.getValue();

            LoanResponse loan = fetchLoan(loanId);
            // Loan may have been deleted, or already fully closed out — either way it
            // no longer belongs in an active collections workqueue.
            if (loan == null || "CLOSED".equals(loan.getStatus())) continue;

            long maxDpd = overduePayments.stream()
                    .mapToLong(p -> ChronoUnit.DAYS.between(p.getDueDate(), today))
                    .max().orElse(0);
            CollectionBucket itemBucket = CollectionBucket.classify(maxDpd);
            if (bucket != null && bucket != itemBucket) continue;

            CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId).orElse(null);
            if (assignedToUserId != null
                    && (collectionCase == null || !assignedToUserId.equals(collectionCase.getAssignedToUserId()))) {
                continue;
            }

            BigDecimal totalOverdue = overduePayments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDate oldestDue = overduePayments.stream()
                    .map(Payment::getDueDate)
                    .min(LocalDate::compareTo)
                    .orElse(today);
            CustomerResponse customer = fetchCustomer(loan.getCustomerId());

            items.add(CollectionWorkqueueItemResponse.builder()
                    .loanId(loanId)
                    .customerId(loan.getCustomerId())
                    .customerName(loan.getCustomerName() != null ? loan.getCustomerName()
                            : customer != null ? customer.getFullName() : null)
                    .customerPhone(customer != null ? customer.getPhone() : null)
                    .customerEmail(customer != null ? customer.getEmail() : null)
                    .principal(loan.getPrincipal())
                    .outstandingBalance(loan.getOutstandingBalance())
                    .totalOverdueAmount(totalOverdue)
                    .oldestDueDate(oldestDue)
                    .maxDpd(maxDpd)
                    .bucket(itemBucket)
                    .overdueInstallmentCount(overduePayments.size())
                    .caseStatus(collectionCase != null ? collectionCase.getStatus() : null)
                    .assignedToUserId(collectionCase != null ? collectionCase.getAssignedToUserId() : null)
                    .lastContactAt(collectionCase != null ? collectionCase.getLastContactAt() : null)
                    .nextFollowUpAt(collectionCase != null ? collectionCase.getNextFollowUpAt() : null)
                    .build());
        }

        items.sort(Comparator.comparingLong(CollectionWorkqueueItemResponse::getMaxDpd).reversed());
        return items;
    }

    @Override
    public CollectionCaseResponse getCase(Long loanId) {
        return collectionCaseRepository.findByLoanId(loanId)
                .map(this::toCaseResponse)
                .orElseGet(() -> CollectionCaseResponse.builder()
                        .loanId(loanId)
                        .status(com.example.payment.entity.CollectionCaseStatus.OPEN)
                        .build());
    }

    @Override
    public CollectionCaseResponse assign(Long loanId, AssignCollectionCaseRequest request, String assignedBy) {
        loanClient.getById(loanId);
        CollectionCase collectionCase = findOrCreateCase(loanId);
        collectionCase.setAssignedToUserId(request.getUserId());
        collectionCaseRepository.save(collectionCase);

        CollectionCaseAssignment assignment = CollectionCaseAssignment.builder()
                .collectionCase(collectionCase)
                .assignedToUserId(request.getUserId())
                .assignedBy(assignedBy)
                .note(request.getNote())
                .build();
        collectionCaseAssignmentRepository.save(assignment);

        return toCaseResponse(collectionCase);
    }

    @Override
    public CollectionCaseResponse updateStatus(Long loanId, UpdateCollectionCaseStatusRequest request, String changedBy) {
        loanClient.getById(loanId);
        CollectionCase collectionCase = findOrCreateCase(loanId);
        var fromStatus = collectionCase.getStatus();
        collectionCase.setStatus(request.getStatus());
        collectionCaseRepository.save(collectionCase);

        CollectionStatusHistory history = CollectionStatusHistory.builder()
                .collectionCase(collectionCase)
                .fromStatus(fromStatus)
                .toStatus(request.getStatus())
                .changedBy(changedBy)
                .note(request.getNote())
                .build();
        collectionStatusHistoryRepository.save(history);

        return toCaseResponse(collectionCase);
    }

    @Override
    public List<CollectionStatusHistoryResponse> listStatusHistory(Long loanId) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId).orElse(null);
        if (collectionCase == null) return List.of();
        return collectionStatusHistoryRepository.findByCollectionCaseIdOrderByCreatedAtDesc(collectionCase.getId())
                .stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Override
    public List<CollectionCaseAssignmentResponse> listAssignments(Long loanId) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId).orElse(null);
        if (collectionCase == null) return List.of();
        return collectionCaseAssignmentRepository.findByCollectionCaseIdOrderByCreatedAtDesc(collectionCase.getId())
                .stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Override
    public List<CollectionActivityResponse> listActivities(Long loanId) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId).orElse(null);
        if (collectionCase == null) return List.of();
        return collectionActivityRepository.findByCollectionCaseIdOrderByCreatedAtDesc(collectionCase.getId()).stream()
                .map(this::toActivityResponse)
                .toList();
    }

    @Override
    public CollectionActivityResponse addActivity(Long loanId, CollectionActivityRequest request, String authorName) {
        loanClient.getById(loanId);
        CollectionCase collectionCase = findOrCreateCase(loanId);
        collectionCase.setLastContactAt(LocalDateTime.now());
        if (request.getFollowUpDate() != null) {
            collectionCase.setNextFollowUpAt(request.getFollowUpDate());
        }
        collectionCaseRepository.save(collectionCase);

        CollectionActivity activity = CollectionActivity.builder()
                .collectionCase(collectionCase)
                .authorName(authorName)
                .contactMethod(request.getContactMethod())
                .outcome(request.getOutcome())
                .note(request.getNote())
                .followUpDate(request.getFollowUpDate())
                .build();
        return toActivityResponse(collectionActivityRepository.save(activity));
    }

    @Override
    public List<CollectionPromiseResponse> listPromises(Long loanId) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId).orElse(null);
        if (collectionCase == null) return List.of();
        return collectionPromiseRepository.findByCollectionCaseIdOrderByCreatedAtDesc(collectionCase.getId()).stream()
                .map(this::toPromiseResponse)
                .toList();
    }

    @Override
    public CollectionPromiseResponse addPromise(Long loanId, CollectionPromiseRequest request, String authorName) {
        loanClient.getById(loanId);
        CollectionCase collectionCase = findOrCreateCase(loanId);
        collectionCaseRepository.save(collectionCase);

        CollectionPromise promise = CollectionPromise.builder()
                .collectionCase(collectionCase)
                .promisedAmount(request.getPromisedAmount())
                .promisedDate(request.getPromisedDate())
                .status(PromiseStatus.PENDING)
                .notes(request.getNotes())
                .createdByName(authorName)
                .build();
        return toPromiseResponse(collectionPromiseRepository.save(promise));
    }

    @Override
    public CollectionPromiseResponse resolvePromise(Long loanId, Long promiseId, ResolveCollectionPromiseRequest request) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Collection case not found for loan " + loanId));
        CollectionPromise promise = collectionPromiseRepository.findByIdAndCollectionCaseId(promiseId, collectionCase.getId())
                .orElseThrow(() -> new EntityNotFoundException("Promise " + promiseId + " not found for loan " + loanId));

        promise.setStatus(request.getStatus());
        promise.setAmountPaid(request.getAmountPaid());
        promise.setResolvedAt(LocalDateTime.now());
        return toPromiseResponse(collectionPromiseRepository.save(promise));
    }

    @Override
    public List<CollectionLetterResponse> listLetters(Long loanId) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId).orElse(null);
        if (collectionCase == null) return List.of();
        return collectionLetterRepository.findByCollectionCaseIdOrderByCreatedAtDesc(collectionCase.getId()).stream()
                .map(this::toLetterResponse)
                .toList();
    }

    @Override
    public CollectionLetterResponse addLetter(Long loanId, CollectionLetterRequest request, String authorName) {
        loanClient.getById(loanId);
        CollectionCase collectionCase = findOrCreateCase(loanId);
        collectionCaseRepository.save(collectionCase);

        CollectionLetter letter = CollectionLetter.builder()
                .collectionCase(collectionCase)
                .letterType(request.getLetterType())
                .deliveryMethod(request.getDeliveryMethod())
                .status(LetterStatus.DRAFT)
                .recipientAddress(request.getRecipientAddress())
                .content(request.getContent())
                .generatedByName(authorName)
                .build();
        return toLetterResponse(collectionLetterRepository.save(letter));
    }

    @Override
    public CollectionLetterResponse updateLetterStatus(Long loanId, Long letterId, UpdateCollectionLetterStatusRequest request) {
        CollectionCase collectionCase = collectionCaseRepository.findByLoanId(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Collection case not found for loan " + loanId));
        CollectionLetter letter = collectionLetterRepository.findByIdAndCollectionCaseId(letterId, collectionCase.getId())
                .orElseThrow(() -> new EntityNotFoundException("Letter " + letterId + " not found for loan " + loanId));

        letter.setStatus(request.getStatus());
        if (request.getStatus() == LetterStatus.SENT && letter.getSentAt() == null) {
            letter.setSentAt(LocalDateTime.now());
        }
        return toLetterResponse(collectionLetterRepository.save(letter));
    }

    private CollectionCase findOrCreateCase(Long loanId) {
        return collectionCaseRepository.findByLoanId(loanId)
                .orElseGet(() -> CollectionCase.builder().loanId(loanId).build());
    }

    // The workqueue lists every loan with an overdue payment, but a Feign call
    // failing for one loan (deleted, upstream hiccup) shouldn't take the whole
    // list down — skip that row instead of propagating the exception.
    private LoanResponse fetchLoan(Long loanId) {
        try {
            return loanClient.getById(loanId).getData();
        } catch (FeignException ex) {
            return null;
        }
    }

    private CustomerResponse fetchCustomer(Long customerId) {
        try {
            return customerClient.getById(customerId).getData();
        } catch (FeignException ex) {
            return null;
        }
    }

    private CollectionCaseResponse toCaseResponse(CollectionCase collectionCase) {
        return CollectionCaseResponse.builder()
                .loanId(collectionCase.getLoanId())
                .status(collectionCase.getStatus())
                .assignedToUserId(collectionCase.getAssignedToUserId())
                .lastContactAt(collectionCase.getLastContactAt())
                .nextFollowUpAt(collectionCase.getNextFollowUpAt())
                .build();
    }

    private CollectionStatusHistoryResponse toStatusHistoryResponse(CollectionStatusHistory history) {
        return CollectionStatusHistoryResponse.builder()
                .id(history.getId())
                .loanId(history.getCollectionCase().getLoanId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedBy(history.getChangedBy())
                .note(history.getNote())
                .changedAt(history.getCreatedAt())
                .build();
    }

    private CollectionCaseAssignmentResponse toAssignmentResponse(CollectionCaseAssignment assignment) {
        return CollectionCaseAssignmentResponse.builder()
                .id(assignment.getId())
                .loanId(assignment.getCollectionCase().getLoanId())
                .assignedToUserId(assignment.getAssignedToUserId())
                .assignedBy(assignment.getAssignedBy())
                .note(assignment.getNote())
                .assignedAt(assignment.getCreatedAt())
                .build();
    }

    private CollectionActivityResponse toActivityResponse(CollectionActivity activity) {
        return CollectionActivityResponse.builder()
                .id(activity.getId())
                .loanId(activity.getCollectionCase().getLoanId())
                .authorName(activity.getAuthorName())
                .contactMethod(activity.getContactMethod())
                .outcome(activity.getOutcome())
                .note(activity.getNote())
                .followUpDate(activity.getFollowUpDate())
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private CollectionPromiseResponse toPromiseResponse(CollectionPromise promise) {
        return CollectionPromiseResponse.builder()
                .id(promise.getId())
                .loanId(promise.getCollectionCase().getLoanId())
                .promisedAmount(promise.getPromisedAmount())
                .promisedDate(promise.getPromisedDate())
                .status(promise.getStatus())
                .amountPaid(promise.getAmountPaid())
                .notes(promise.getNotes())
                .createdByName(promise.getCreatedByName())
                .resolvedAt(promise.getResolvedAt())
                .createdAt(promise.getCreatedAt())
                .build();
    }

    private CollectionLetterResponse toLetterResponse(CollectionLetter letter) {
        return CollectionLetterResponse.builder()
                .id(letter.getId())
                .loanId(letter.getCollectionCase().getLoanId())
                .letterType(letter.getLetterType())
                .deliveryMethod(letter.getDeliveryMethod())
                .status(letter.getStatus())
                .recipientAddress(letter.getRecipientAddress())
                .content(letter.getContent())
                .generatedByName(letter.getGeneratedByName())
                .sentAt(letter.getSentAt())
                .createdAt(letter.getCreatedAt())
                .build();
    }
}
