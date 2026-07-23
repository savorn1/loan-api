package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplicationApprovalRequest;
import com.example.loan.dto.ApplicationApprovalResponse;
import com.example.loan.dto.ApplicationDocumentRequest;
import com.example.loan.dto.ApplicationDocumentResponse;
import com.example.loan.dto.ApplicationNoteRequest;
import com.example.loan.dto.ApplicationNoteResponse;
import com.example.loan.dto.ApplicationRequest;
import com.example.loan.dto.ApplicationResponse;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.entity.Application;
import com.example.loan.entity.ApplicationApproval;
import com.example.loan.entity.ApplicationDocument;
import com.example.loan.entity.ApplicationNote;
import com.example.loan.entity.ApplicationStatus;
import com.example.loan.entity.ApprovalDecision;
import com.example.loan.entity.DocumentStatus;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import com.example.loan.exception.AppException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.repository.ApplicationApprovalRepository;
import com.example.loan.repository.ApplicationDocumentRepository;
import com.example.loan.repository.ApplicationNoteRepository;
import com.example.loan.repository.ApplicationRepository;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final ApplicationApprovalRepository applicationApprovalRepository;
    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;

    @Override
    public ApplicationResponse create(ApplicationRequest request) {
        CustomerResponse customer = customerClient.getById(request.getCustomerId());

        Application application = Application.builder()
                .customerId(request.getCustomerId())
                .requestedAmount(request.getRequestedAmount())
                .requestedTermMonths(request.getRequestedTermMonths())
                .purpose(request.getPurpose())
                .status(ApplicationStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        return toResponse(applicationRepository.save(application), customer);
    }

    @Override
    public ApplicationResponse getById(Long id) {
        Application application = findOrThrow(id);
        return toResponse(application, customerClient.getById(application.getCustomerId()));
    }

    @Override
    public PageResponse<ApplicationResponse> getAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(applicationRepository.findAll(pageable)
                .map(application -> toResponse(application, customerClient.getById(application.getCustomerId()))));
    }

    @Override
    public List<ApplicationResponse> getByCustomer(Long customerId) {
        customerClient.getById(customerId);
        return applicationRepository.findByCustomerId(customerId).stream()
                .map(application -> toResponse(application, customerClient.getById(application.getCustomerId())))
                .toList();
    }

    @Override
    public ApplicationResponse update(Long id, ApplicationRequest request) {
        Application application = findOrThrow(id);
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED applications can be edited");
        }
        application.setRequestedAmount(request.getRequestedAmount());
        application.setRequestedTermMonths(request.getRequestedTermMonths());
        application.setPurpose(request.getPurpose());
        CustomerResponse customer = customerClient.getById(application.getCustomerId());
        return toResponse(applicationRepository.save(application), customer);
    }

    @Override
    public ApplicationResponse startReview(Long id) {
        Application application = findOrThrow(id);
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED applications can start review");
        }
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        CustomerResponse customer = customerClient.getById(application.getCustomerId());
        return toResponse(applicationRepository.save(application), customer);
    }

    @Override
    public ApplicationResponse withdraw(Long id) {
        Application application = findOrThrow(id);
        if (application.getStatus() != ApplicationStatus.SUBMITTED && application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED or UNDER_REVIEW applications can be withdrawn");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setDecidedAt(LocalDateTime.now());
        CustomerResponse customer = customerClient.getById(application.getCustomerId());
        return toResponse(applicationRepository.save(application), customer);
    }

    @Override
    public void delete(Long id) {
        Application application = findOrThrow(id);
        if (application.getLoanId() != null) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete an application that already produced a loan");
        }
        applicationRepository.delete(application);
    }

    @Override
    public ApplicationDocumentResponse addDocument(Long id, ApplicationDocumentRequest request) {
        Application application = findOrThrow(id);
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot add documents to a withdrawn application");
        }
        ApplicationDocument document = ApplicationDocument.builder()
                .application(application)
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .status(DocumentStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();
        return toDocumentResponse(applicationDocumentRepository.save(document));
    }

    @Override
    public ApplicationDocumentResponse verifyDocument(Long id, Long documentId) {
        ApplicationDocument document = findDocumentOrThrow(id, documentId);
        document.setStatus(DocumentStatus.VERIFIED);
        return toDocumentResponse(applicationDocumentRepository.save(document));
    }

    @Override
    public ApplicationDocumentResponse rejectDocument(Long id, Long documentId) {
        ApplicationDocument document = findDocumentOrThrow(id, documentId);
        document.setStatus(DocumentStatus.REJECTED);
        return toDocumentResponse(applicationDocumentRepository.save(document));
    }

    @Override
    public void deleteDocument(Long id, Long documentId) {
        applicationDocumentRepository.delete(findDocumentOrThrow(id, documentId));
    }

    @Override
    public ApplicationNoteResponse addNote(Long id, ApplicationNoteRequest request) {
        Application application = findOrThrow(id);
        ApplicationNote note = ApplicationNote.builder()
                .application(application)
                .authorName(currentUsername())
                .note(request.getNote())
                .build();
        return toNoteResponse(applicationNoteRepository.save(note));
    }

    @Override
    @Transactional
    public ApplicationResponse addApproval(Long id, ApplicationApprovalRequest request) {
        Application application = findOrThrow(id);
        if (application.getStatus() != ApplicationStatus.SUBMITTED && application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED or UNDER_REVIEW applications can receive a decision");
        }

        LocalDateTime now = LocalDateTime.now();
        String username = currentUsername();

        if (request.getDecision() == ApprovalDecision.APPROVED
                && (request.getApprovedAmount() == null || request.getApprovedInterestRate() == null || request.getApprovedTermMonths() == null)) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "approvedAmount, approvedInterestRate and approvedTermMonths are all required when approving");
        }

        ApplicationApproval approval = ApplicationApproval.builder()
                .application(application)
                .approverName(username)
                .decision(request.getDecision())
                .approvedAmount(request.getApprovedAmount())
                .approvedInterestRate(request.getApprovedInterestRate())
                .approvedTermMonths(request.getApprovedTermMonths())
                .comments(request.getComments())
                .decidedAt(now)
                .build();
        applicationApprovalRepository.save(approval);

        application.setDecidedAt(now);
        if (request.getDecision() == ApprovalDecision.APPROVED) {
            Loan loan = Loan.builder()
                    .customerId(application.getCustomerId())
                    .principal(request.getApprovedAmount())
                    .interestRate(request.getApprovedInterestRate())
                    .termMonths(request.getApprovedTermMonths())
                    .purpose(application.getPurpose())
                    .status(LoanStatus.APPROVED)
                    .approvedAt(now)
                    .build();
            loan = loanRepository.save(loan);
            application.setLoanId(loan.getId());
            application.setStatus(ApplicationStatus.APPROVED);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
        }
        applicationRepository.save(application);

        return toResponse(application, customerClient.getById(application.getCustomerId()));
    }

    private Application findOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
    }

    private ApplicationDocument findDocumentOrThrow(Long applicationId, Long documentId) {
        ApplicationDocument document = applicationDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Application document", documentId));
        if (!document.getApplication().getId().equals(applicationId)) {
            throw new ResourceNotFoundException("Application document", documentId);
        }
        return document;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private ApplicationDocumentResponse toDocumentResponse(ApplicationDocument document) {
        return ApplicationDocumentResponse.builder()
                .id(document.getId())
                .applicationId(document.getApplication().getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .status(document.getStatus())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    private ApplicationNoteResponse toNoteResponse(ApplicationNote note) {
        return ApplicationNoteResponse.builder()
                .id(note.getId())
                .applicationId(note.getApplication().getId())
                .authorName(note.getAuthorName())
                .note(note.getNote())
                .createdAt(note.getCreatedAt())
                .build();
    }

    private ApplicationApprovalResponse toApprovalResponse(ApplicationApproval approval) {
        return ApplicationApprovalResponse.builder()
                .id(approval.getId())
                .applicationId(approval.getApplication().getId())
                .approverName(approval.getApproverName())
                .decision(approval.getDecision())
                .approvedAmount(approval.getApprovedAmount())
                .approvedInterestRate(approval.getApprovedInterestRate())
                .approvedTermMonths(approval.getApprovedTermMonths())
                .comments(approval.getComments())
                .decidedAt(approval.getDecidedAt())
                .build();
    }

    private ApplicationResponse toResponse(Application application, CustomerResponse customer) {
        List<ApplicationDocumentResponse> documents = applicationDocumentRepository
                .findByApplicationIdOrderByUploadedAtAsc(application.getId()).stream()
                .map(this::toDocumentResponse)
                .toList();
        List<ApplicationNoteResponse> notes = applicationNoteRepository
                .findByApplicationIdOrderByCreatedAtAsc(application.getId()).stream()
                .map(this::toNoteResponse)
                .toList();
        List<ApplicationApprovalResponse> approvals = applicationApprovalRepository
                .findByApplicationIdOrderByDecidedAtAsc(application.getId()).stream()
                .map(this::toApprovalResponse)
                .toList();

        return ApplicationResponse.builder()
                .id(application.getId())
                .customerId(application.getCustomerId())
                .customerName(customer != null ? customer.getFirstName() + " " + customer.getLastName() : null)
                .requestedAmount(application.getRequestedAmount())
                .requestedTermMonths(application.getRequestedTermMonths())
                .purpose(application.getPurpose())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .decidedAt(application.getDecidedAt())
                .loanId(application.getLoanId())
                .documents(documents)
                .notes(notes)
                .approvals(approvals)
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
