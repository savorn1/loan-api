package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.GroupLoanApplicationApprovalRequest;
import com.example.loan.dto.GroupLoanApplicationDocumentRequest;
import com.example.loan.dto.GroupLoanApplicationDocumentResponse;
import com.example.loan.dto.GroupLoanApplicationRequest;
import com.example.loan.dto.GroupLoanApplicationResponse;
import com.example.loan.dto.GroupLoanApprovalMemberDecisionRequest;
import com.example.loan.dto.GroupLoanMemberLineResponse;
import com.example.loan.dto.GroupLoanMemberRequest;
import com.example.loan.entity.ApprovalDecision;
import com.example.loan.entity.DocumentStatus;
import com.example.loan.entity.Group;
import com.example.loan.entity.GroupLoanApplication;
import com.example.loan.entity.GroupLoanApplicationDocument;
import com.example.loan.entity.GroupLoanApplicationStatus;
import com.example.loan.entity.GroupLoanMember;
import com.example.loan.entity.GroupMemberStatus;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import com.example.loan.entity.TermUnit;
import com.example.loan.exception.AppException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.repository.GroupLoanApplicationDocumentRepository;
import com.example.loan.repository.GroupLoanApplicationRepository;
import com.example.loan.repository.GroupLoanMemberRepository;
import com.example.loan.repository.GroupMemberRepository;
import com.example.loan.repository.GroupRepository;
import com.example.loan.repository.LoanRepository;
import com.example.loan.service.GroupLoanApplicationService;
import com.example.storage.FileStorageService;
import com.example.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupLoanApplicationServiceImpl implements GroupLoanApplicationService {

    private final GroupLoanApplicationRepository applicationRepository;
    private final GroupLoanMemberRepository memberRepository;
    private final GroupLoanApplicationDocumentRepository documentRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public GroupLoanApplicationResponse create(GroupLoanApplicationRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", request.getGroupId()));

        for (GroupLoanMemberRequest memberRequest : request.getMembers()) {
            if (!groupMemberRepository.existsByGroupIdAndCustomerIdAndStatus(
                    group.getId(), memberRequest.getCustomerId(), GroupMemberStatus.ACTIVE)) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "Customer " + memberRequest.getCustomerId() + " is not an active member of this group");
            }
        }

        GroupLoanApplication application = GroupLoanApplication.builder()
                .group(group)
                .branchId(group.getBranchId())
                .purpose(request.getPurpose())
                .status(GroupLoanApplicationStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();
        application = applicationRepository.save(application);
        String applicationNo = generateApplicationNo(application);
        applicationRepository.updateApplicationNo(application.getId(), applicationNo);
        application.setApplicationNo(applicationNo);

        for (GroupLoanMemberRequest memberRequest : request.getMembers()) {
            GroupLoanMember member = GroupLoanMember.builder()
                    .application(application)
                    .customerId(memberRequest.getCustomerId())
                    .requestedAmount(memberRequest.getRequestedAmount())
                    .requestedTermMonths(memberRequest.getRequestedTermMonths())
                    .requestedTermUnit(memberRequest.getRequestedTermUnit())
                    .build();
            memberRepository.save(member);
        }

        return toResponse(application);
    }

    @Override
    public GroupLoanApplicationResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<GroupLoanApplicationResponse> getAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(applicationRepository.findAll(pageable).map(this::toResponse));
    }

    @Override
    public List<GroupLoanApplicationResponse> getByGroup(Long groupId) {
        return applicationRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GroupLoanApplicationResponse startReview(Long id) {
        GroupLoanApplication application = findOrThrow(id);
        if (application.getStatus() != GroupLoanApplicationStatus.SUBMITTED) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED applications can start review");
        }
        application.setStatus(GroupLoanApplicationStatus.UNDER_REVIEW);
        return toResponse(applicationRepository.save(application));
    }

    @Override
    public GroupLoanApplicationResponse cancel(Long id) {
        GroupLoanApplication application = findOrThrow(id);
        if (application.getStatus() != GroupLoanApplicationStatus.SUBMITTED
                && application.getStatus() != GroupLoanApplicationStatus.UNDER_REVIEW) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED or UNDER_REVIEW applications can be cancelled");
        }
        application.setStatus(GroupLoanApplicationStatus.WITHDRAWN);
        application.setDecidedAt(LocalDateTime.now());
        return toResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public GroupLoanApplicationResponse addApproval(Long id, GroupLoanApplicationApprovalRequest request) {
        GroupLoanApplication application = findOrThrow(id);
        if (application.getStatus() != GroupLoanApplicationStatus.SUBMITTED
                && application.getStatus() != GroupLoanApplicationStatus.UNDER_REVIEW) {
            throw new AppException(HttpStatus.CONFLICT, "Only SUBMITTED or UNDER_REVIEW applications can receive a decision");
        }

        LocalDateTime now = LocalDateTime.now();

        if (request.getDecision() == ApprovalDecision.APPROVED) {
            List<GroupLoanMember> members = memberRepository.findByApplicationIdOrderByIdAsc(id);
            if (request.getMembers() == null || request.getMembers().size() != members.size()) {
                throw new AppException(HttpStatus.BAD_REQUEST,
                        "A decision is required for every member of the application when approving");
            }
            for (GroupLoanApprovalMemberDecisionRequest decision : request.getMembers()) {
                if (decision.getApprovedAmount() == null || decision.getApprovedInterestRate() == null
                        || decision.getApprovedTermMonths() == null) {
                    throw new AppException(HttpStatus.BAD_REQUEST,
                            "approvedAmount, approvedInterestRate and approvedTermMonths are all required when approving");
                }
                GroupLoanMember member = memberRepository.findByApplicationIdAndCustomerId(id, decision.getCustomerId())
                        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                                "Customer " + decision.getCustomerId() + " is not a member of this application"));

                // Falls back to what the member originally requested if the approver
                // doesn't explicitly change it — same convention as the individual
                // application approval flow.
                TermUnit approvedTermUnit = decision.getApprovedTermUnit() != null
                        ? decision.getApprovedTermUnit() : member.getRequestedTermUnit();

                member.setApprovedAmount(decision.getApprovedAmount());
                member.setApprovedInterestRate(decision.getApprovedInterestRate());
                member.setApprovedTermMonths(decision.getApprovedTermMonths());
                member.setApprovedTermUnit(approvedTermUnit);

                Loan loan = Loan.builder()
                        .customerId(member.getCustomerId())
                        .branchId(application.getBranchId())
                        .principal(decision.getApprovedAmount())
                        .interestRate(decision.getApprovedInterestRate())
                        .termMonths(decision.getApprovedTermMonths())
                        .termUnit(approvedTermUnit)
                        .purpose(application.getPurpose())
                        .status(LoanStatus.APPROVED)
                        .approvedAt(now)
                        .build();
                loan = loanRepository.save(loan);
                member.setLoanId(loan.getId());
                memberRepository.save(member);
            }
            application.setStatus(GroupLoanApplicationStatus.APPROVED);
        } else {
            application.setStatus(GroupLoanApplicationStatus.REJECTED);
        }

        application.setDecidedAt(now);
        return toResponse(applicationRepository.save(application));
    }

    @Override
    public GroupLoanApplicationDocumentResponse addDocument(Long id, GroupLoanApplicationDocumentRequest request) {
        GroupLoanApplication application = findOrThrow(id);
        assertNotWithdrawn(application);
        GroupLoanApplicationDocument document = GroupLoanApplicationDocument.builder()
                .application(application)
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .status(DocumentStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();
        return toDocumentResponse(documentRepository.save(document));
    }

    @Override
    public GroupLoanApplicationDocumentResponse uploadDocument(Long id, String documentType, MultipartFile file) {
        GroupLoanApplication application = findOrThrow(id);
        assertNotWithdrawn(application);
        StoredFile stored = fileStorageService.upload(file, "group-applications/" + id);
        GroupLoanApplicationDocument document = GroupLoanApplicationDocument.builder()
                .application(application)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .fileUrl(stored.url())
                .status(DocumentStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();
        return toDocumentResponse(documentRepository.save(document));
    }

    private void assertNotWithdrawn(GroupLoanApplication application) {
        if (application.getStatus() == GroupLoanApplicationStatus.WITHDRAWN) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot add documents to a withdrawn application");
        }
    }

    @Override
    public GroupLoanApplicationDocumentResponse verifyDocument(Long id, Long documentId) {
        GroupLoanApplicationDocument document = findDocumentOrThrow(id, documentId);
        document.setStatus(DocumentStatus.VERIFIED);
        return toDocumentResponse(documentRepository.save(document));
    }

    @Override
    public GroupLoanApplicationDocumentResponse rejectDocument(Long id, Long documentId) {
        GroupLoanApplicationDocument document = findDocumentOrThrow(id, documentId);
        document.setStatus(DocumentStatus.REJECTED);
        return toDocumentResponse(documentRepository.save(document));
    }

    @Override
    public void deleteDocument(Long id, Long documentId) {
        documentRepository.delete(findDocumentOrThrow(id, documentId));
    }

    private GroupLoanApplication findOrThrow(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group loan application", id));
    }

    private GroupLoanApplicationDocument findDocumentOrThrow(Long applicationId, Long documentId) {
        GroupLoanApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Group loan application document", documentId));
        if (!document.getApplication().getId().equals(applicationId)) {
            throw new ResourceNotFoundException("Group loan application document", documentId);
        }
        return document;
    }

    private GroupLoanApplicationDocumentResponse toDocumentResponse(GroupLoanApplicationDocument document) {
        return GroupLoanApplicationDocumentResponse.builder()
                .id(document.getId())
                .applicationId(document.getApplication().getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .status(document.getStatus())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    private GroupLoanMemberLineResponse toMemberLine(GroupLoanMember member) {
        CustomerResponse customer = customerClient.getById(member.getCustomerId()).getData();
        return GroupLoanMemberLineResponse.builder()
                .customerId(member.getCustomerId())
                .customerName(customer != null ? customer.getFirstName() + " " + customer.getLastName() : null)
                .requestedAmount(member.getRequestedAmount())
                .requestedTermMonths(member.getRequestedTermMonths())
                .requestedTermUnit(member.getRequestedTermUnit())
                .approvedAmount(member.getApprovedAmount())
                .approvedInterestRate(member.getApprovedInterestRate())
                .approvedTermMonths(member.getApprovedTermMonths())
                .approvedTermUnit(member.getApprovedTermUnit())
                .loanId(member.getLoanId())
                .build();
    }

    private GroupLoanApplicationResponse toResponse(GroupLoanApplication application) {
        List<GroupLoanMemberLineResponse> members = memberRepository
                .findByApplicationIdOrderByIdAsc(application.getId()).stream()
                .map(this::toMemberLine)
                .toList();
        List<GroupLoanApplicationDocumentResponse> documents = documentRepository
                .findByApplicationIdOrderByUploadedAtAsc(application.getId()).stream()
                .map(this::toDocumentResponse)
                .toList();

        return GroupLoanApplicationResponse.builder()
                .id(application.getId())
                .applicationNo(application.getApplicationNo())
                .groupId(application.getGroup().getId())
                .groupName(application.getGroup().getName())
                .branchId(application.getBranchId())
                .purpose(application.getPurpose())
                .status(application.getStatus())
                .members(members)
                .documents(documents)
                .submittedAt(application.getSubmittedAt())
                .decidedAt(application.getDecidedAt())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private String generateApplicationNo(GroupLoanApplication application) {
        String datePart = application.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "GAPP-" + datePart + "-" + String.format("%06d", application.getId());
    }
}
