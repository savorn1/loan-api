package com.example.loan.service.impl;

import com.example.loan.client.AccountingClient;
import com.example.loan.client.CustomerClient;
import com.example.loan.client.PaymentClient;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.DisbursementReasonRequest;
import com.example.loan.dto.GenerateScheduleRequest;
import com.example.loan.dto.JournalEntryGenerateRequest;
import com.example.loan.dto.JournalEntryResponse;
import com.example.loan.dto.LoanAdjustmentRequest;
import com.example.loan.dto.LoanAdjustmentResponse;
import com.example.loan.dto.LoanCollateralRequest;
import com.example.loan.dto.LoanCollateralResponse;
import com.example.loan.dto.LoanDisbursementRequest;
import com.example.loan.dto.LoanDisbursementResponse;
import com.example.loan.dto.LoanDocumentRequest;
import com.example.loan.dto.LoanDocumentResponse;
import com.example.loan.dto.LoanDocumentStatusUpdateRequest;
import com.example.loan.dto.LoanNoteRequest;
import com.example.loan.dto.LoanNoteResponse;
import com.example.loan.dto.LoanFeeRequest;
import com.example.loan.dto.LoanFeeResponse;
import com.example.loan.dto.LoanGuarantorRequest;
import com.example.loan.dto.LoanGuarantorResponse;
import com.example.loan.dto.LoanInterestRequest;
import com.example.loan.dto.LoanInterestResponse;
import com.example.loan.dto.LoanPaymentDetailResponse;
import com.example.loan.dto.LoanPaymentRequest;
import com.example.loan.dto.LoanPaymentResponse;
import com.example.loan.dto.LoanPayoffQuoteResponse;
import com.example.loan.dto.LoanPayoffRequest;
import com.example.loan.dto.LoanPenaltyRequest;
import com.example.loan.dto.LoanPenaltyResponse;
import com.example.loan.dto.LoanRefinanceRequest;
import com.example.loan.dto.LoanRefinanceResponse;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.LoanRestructureRequest;
import com.example.loan.dto.LoanRestructureResponse;
import com.example.loan.dto.LoanScheduleInstallmentResponse;
import com.example.loan.dto.LoanScheduleResponse;
import com.example.loan.dto.LoanSettlementRequest;
import com.example.loan.dto.LoanSettlementResponse;
import com.example.loan.dto.LoanStatusHistoryResponse;
import com.example.loan.dto.LoanTransactionResponse;
import com.example.loan.dto.LoanWriteoffRecoveryRequest;
import com.example.loan.dto.LoanWriteoffRecoveryResponse;
import com.example.loan.dto.LoanWriteoffRequest;
import com.example.loan.dto.LoanWriteoffResponse;
import com.example.loan.dto.ScheduleInstallmentRequest;
import com.example.loan.entity.AdjustmentType;
import com.example.loan.entity.CollateralStatus;
import com.example.loan.entity.DisbursementMethod;
import com.example.loan.entity.DisbursementStatus;
import com.example.loan.entity.FeeStatus;
import com.example.loan.entity.GuarantorStatus;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanAdjustment;
import com.example.loan.entity.LoanCollateral;
import com.example.loan.entity.LoanDocument;
import com.example.loan.entity.LoanNote;
import com.example.loan.entity.LoanDisbursement;
import com.example.loan.entity.LoanFee;
import com.example.loan.entity.LoanGuarantor;
import com.example.loan.entity.LoanInterestAccrual;
import com.example.loan.entity.LoanPayment;
import com.example.loan.entity.LoanPaymentDetail;
import com.example.loan.entity.LoanPenalty;
import com.example.loan.entity.LoanRefinance;
import com.example.loan.entity.LoanRestructure;
import com.example.loan.entity.LoanSchedule;
import com.example.loan.entity.LoanScheduleInstallment;
import com.example.loan.entity.LoanSettlement;
import com.example.loan.entity.LoanStatus;
import com.example.loan.entity.LoanStatusHistory;
import com.example.loan.entity.LoanTransaction;
import com.example.loan.entity.LoanWriteoff;
import com.example.loan.entity.PenaltyStatus;
import com.example.loan.entity.ScheduleInstallmentStatus;
import com.example.loan.entity.ScheduleStatus;
import com.example.loan.entity.SettlementStatus;
import com.example.loan.entity.TransactionType;
import com.example.loan.entity.WriteoffStatus;
import com.example.loan.exception.AppException;
import com.example.loan.exception.ResourceNotFoundException;
import com.example.loan.repository.LoanAdjustmentRepository;
import com.example.loan.repository.LoanCollateralRepository;
import com.example.loan.repository.LoanDocumentRepository;
import com.example.loan.repository.LoanNoteRepository;
import com.example.loan.repository.LoanDisbursementRepository;
import com.example.loan.repository.LoanFeeRepository;
import com.example.loan.repository.LoanGuarantorRepository;
import com.example.loan.repository.LoanInterestAccrualRepository;
import com.example.loan.repository.LoanPaymentDetailRepository;
import com.example.loan.repository.LoanPaymentRepository;
import com.example.loan.repository.LoanPenaltyRepository;
import com.example.loan.repository.LoanRefinanceRepository;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.LoanRestructureRepository;
import com.example.loan.repository.LoanScheduleInstallmentRepository;
import com.example.loan.repository.LoanScheduleRepository;
import com.example.loan.repository.LoanSettlementRepository;
import com.example.loan.repository.LoanStatusHistoryRepository;
import com.example.loan.repository.LoanTransactionRepository;
import com.example.loan.entity.LoanWriteoffRecovery;
import com.example.loan.repository.LoanWriteoffRecoveryRepository;
import com.example.loan.repository.LoanWriteoffRepository;
import com.example.loan.service.LoanService;
import com.example.loan.util.AmortizationCalculator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final AccountingClient accountingClient;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;
    private final LoanDisbursementRepository loanDisbursementRepository;
    private final LoanGuarantorRepository loanGuarantorRepository;
    private final LoanCollateralRepository loanCollateralRepository;
    private final LoanDocumentRepository loanDocumentRepository;
    private final LoanNoteRepository loanNoteRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanScheduleInstallmentRepository loanScheduleInstallmentRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final LoanPaymentDetailRepository loanPaymentDetailRepository;
    private final LoanInterestAccrualRepository loanInterestAccrualRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final LoanFeeRepository loanFeeRepository;
    private final LoanRestructureRepository loanRestructureRepository;
    private final LoanRefinanceRepository loanRefinanceRepository;
    private final LoanSettlementRepository loanSettlementRepository;
    private final LoanWriteoffRepository loanWriteoffRepository;
    private final LoanWriteoffRecoveryRepository loanWriteoffRecoveryRepository;
    private final LoanAdjustmentRepository loanAdjustmentRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public LoanResponse create(LoanRequest request) {
        CustomerResponse customer = customerClient.getById(request.getCustomerId()).getData();

        Loan loan = Loan.builder()
                .customerId(request.getCustomerId())
                .branchId(customer != null ? customer.getBranchId() : null)
                .principal(request.getPrincipal())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .purpose(request.getPurpose())
                .build();

        Loan saved = loanRepository.save(loan);
        String loanNo = generateLoanNo(saved);
        loanRepository.updateLoanNo(saved.getId(), loanNo);
        saved.setLoanNo(loanNo);
        recordStatusHistory(saved, null, LoanStatus.PENDING, null);
        return toResponse(saved, customer);
    }

    @Override
    public LoanResponse getById(Long id) {
        Loan loan = findOrThrow(id);
        CustomerResponse customer = customerClient.getById(loan.getCustomerId()).getData();
        return toResponse(loan, customer);
    }

    @Override
    public PageResponse<LoanResponse> getAll(int page, int size, String sortBy, String sortOrder,
                                              Long customerId, Long branchId,
                                              BigDecimal minPrincipal, BigDecimal maxPrincipal,
                                              LocalDate dateFrom, LocalDate dateTo) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        Specification<Loan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (branchId != null) {
                predicates.add(cb.equal(root.get("branchId"), branchId));
            }
            if (minPrincipal != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("principal"), minPrincipal));
            }
            if (maxPrincipal != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("principal"), maxPrincipal));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), dateTo.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(loanRepository.findAll(spec, pageable)
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId()).getData())));
    }

    @Override
    public List<LoanResponse> getByCustomer(Long customerId) {
        customerClient.getById(customerId);
        return loanRepository.findByCustomerId(customerId).stream()
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId()).getData()))
                .toList();
    }

    @Override
    public LoanResponse approve(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING loans can be approved");
        }
        LoanStatus previousStatus = loan.getStatus();
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);
        recordStatusHistory(saved, previousStatus, LoanStatus.APPROVED, null);
        CustomerResponse customer = customerClient.getById(saved.getCustomerId()).getData();
        return toResponse(saved, customer);
    }

    @Override
    public LoanResponse reject(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING loans can be rejected");
        }
        LoanStatus previousStatus = loan.getStatus();
        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);
        recordStatusHistory(saved, previousStatus, LoanStatus.REJECTED, null);
        CustomerResponse customer = customerClient.getById(saved.getCustomerId()).getData();
        return toResponse(saved, customer);
    }

    @Override
    @Transactional
    public LoanResponse disburse(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new AppException(HttpStatus.CONFLICT, "Only APPROVED loans can be disbursed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate disbursementDate = now.toLocalDate();

        List<AmortizationCalculator.Installment> schedule = AmortizationCalculator.generateSchedule(
                loan.getPrincipal(), loan.getInterestRate(), loan.getTermMonths(), disbursementDate);
        BigDecimal emi = AmortizationCalculator.calculateEmi(
                loan.getPrincipal(), loan.getInterestRate(), loan.getTermMonths());
        BigDecimal totalOutstanding = schedule.stream()
                .map(AmortizationCalculator.Installment::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ScheduleInstallmentRequest> installmentRequests = schedule.stream()
                .map(i -> ScheduleInstallmentRequest.builder()
                        .installmentNumber(i.installmentNumber())
                        .dueDate(i.dueDate())
                        .principalComponent(i.principalComponent())
                        .interestComponent(i.interestComponent())
                        .amount(i.amount())
                        .build())
                .toList();

        paymentClient.createSchedule(GenerateScheduleRequest.builder()
                .loanId(loan.getId())
                .installments(installmentRequests)
                .build());

        LoanStatus previousStatus = loan.getStatus();
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(now);
        loan.setMaturityDate(disbursementDate.plusMonths(loan.getTermMonths()));
        loan.setMonthlyInstallment(emi);
        loan.setOutstandingBalance(totalOutstanding);
        Loan saved = loanRepository.save(loan);
        recordStatusHistory(saved, previousStatus, LoanStatus.ACTIVE, null);
        generateAndPersistSchedule(saved, schedule, saved.getPrincipal());
        recordTransaction(saved, TransactionType.DISBURSEMENT, saved.getPrincipal(), disbursementDate,
                "Loan", saved.getId(), null);

        CustomerResponse customer = customerClient.getById(saved.getCustomerId()).getData();
        return toResponse(saved, customer);
    }

    @Override
    @Transactional
    public LoanResponse applyPayment(Long id, ApplyPaymentRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Payments can only be applied to ACTIVE loans");
        }
        LocalDate paymentDate = LocalDate.now();

        // Callers of this action (payment-service's markAsPaid) only ever have an amount,
        // not a channel — DisbursementMethod.OTHER records that honestly instead of
        // guessing. Still creates a real LoanPayment/LoanPaymentDetail trail and allocates
        // against the schedule via allocatePayment, same as addPayment, instead of dumping
        // the whole amount as PRINCIPAL_PAYMENT: a payment that's mostly interest was
        // otherwise posted entirely to principal, understating interest income.
        LoanPayment payment = LoanPayment.builder()
                .loan(loan)
                .amount(request.getAmount())
                .paymentDate(paymentDate)
                .method(DisbursementMethod.OTHER)
                .reference("Applied via legacy apply-payment action")
                .build();
        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        savedPayment.setPaymentNo(generatePaymentNo(savedPayment));
        savedPayment = loanPaymentRepository.save(savedPayment);

        // Same waterfall as addPayment: fees, then penalties, then the schedule. See
        // applyToOutstandingFeesAndPenalties for why only the remainder reduces the balance.
        BigDecimal feesAndPenaltiesApplied =
                applyToOutstandingFeesAndPenalties(loan, request.getAmount(), paymentDate);
        BigDecimal remainingForSchedule = request.getAmount().subtract(feesAndPenaltiesApplied);
        List<LoanPaymentDetail> details = allocatePayment(loan, savedPayment, remainingForSchedule);

        BigDecimal newBalance = loan.getOutstandingBalance().subtract(remainingForSchedule);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        loan.setOutstandingBalance(newBalance);
        boolean closing = newBalance.compareTo(BigDecimal.ZERO) == 0;
        if (closing) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedAt(LocalDateTime.now());
        }
        Loan saved = loanRepository.save(loan);
        if (closing) {
            recordStatusHistory(saved, LoanStatus.ACTIVE, LoanStatus.CLOSED, null);
        }

        BigDecimal totalAllocated = BigDecimal.ZERO;
        for (LoanPaymentDetail detail : details) {
            String description = "Installment #" + detail.getScheduleInstallment().getInstallmentNumber();
            if (detail.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0) {
                recordTransaction(saved, TransactionType.PRINCIPAL_PAYMENT, detail.getPrincipalAmount(),
                        paymentDate, "LoanPaymentDetail", detail.getId(), description);
            }
            if (detail.getInterestAmount().compareTo(BigDecimal.ZERO) > 0) {
                recordTransaction(saved, TransactionType.INTEREST_PAYMENT, detail.getInterestAmount(),
                        paymentDate, "LoanPaymentDetail", detail.getId(), description);
            }
            totalAllocated = totalAllocated.add(detail.getPrincipalAmount()).add(detail.getInterestAmount());
        }
        BigDecimal unallocated = remainingForSchedule.subtract(totalAllocated);
        if (unallocated.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(saved, TransactionType.PRINCIPAL_PAYMENT, unallocated,
                    paymentDate, "LoanPayment", savedPayment.getId(), "Unallocated payment amount");
        }

        CustomerResponse customer = customerClient.getById(saved.getCustomerId()).getData();
        return toResponse(saved, customer);
    }

    @Override
    public LoanResponse update(Long id, LoanRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING loans can be edited");
        }
        CustomerResponse customer = customerClient.getById(request.getCustomerId()).getData();
        loan.setCustomerId(request.getCustomerId());
        loan.setBranchId(customer != null ? customer.getBranchId() : null);
        loan.setPrincipal(request.getPrincipal());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setPurpose(request.getPurpose());
        Loan saved = loanRepository.save(loan);
        return toResponse(saved, customer);
    }

    @Override
    public void delete(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING loans can be deleted");
        }
        loanRepository.delete(loan);
    }

    @Override
    public List<LoanStatusHistoryResponse> getStatusHistory(Long id) {
        findOrThrow(id);
        return loanStatusHistoryRepository.findByLoanIdOrderByChangedAtAsc(id).stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Override
    public LoanDisbursementResponse addDisbursement(Long id, LoanDisbursementRequest request) {
        Loan loan = findOrThrow(id);
        assertWithinPrincipal(loan, request.getAmount(), null);
        LoanDisbursement disbursement = LoanDisbursement.builder()
                .loan(loan)
                .amount(request.getAmount())
                .disbursedDate(request.getDisbursedDate())
                .method(request.getMethod())
                .reference(request.getReference())
                .status(DisbursementStatus.PENDING_APPROVAL)
                .createdBy(currentUsername())
                .build();
        LoanDisbursement saved = loanDisbursementRepository.save(disbursement);
        saved.setDisbursementNo(generateDisbursementNo(saved));
        saved = loanDisbursementRepository.save(saved);
        return toDisbursementResponse(saved);
    }

    @Override
    public List<LoanDisbursementResponse> getDisbursements(Long id) {
        findOrThrow(id);
        return loanDisbursementRepository.findByLoanIdOrderByDisbursedDateAsc(id).stream()
                .map(this::toDisbursementResponse)
                .toList();
    }

    @Override
    public LoanDisbursementResponse updateDisbursement(Long id, Long disbursementId, LoanDisbursementRequest request) {
        LoanDisbursement disbursement = findDisbursementOrThrow(id, disbursementId);
        if (disbursement.getStatus() != DisbursementStatus.PENDING_APPROVAL) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING_APPROVAL disbursements can be edited");
        }
        assertWithinPrincipal(disbursement.getLoan(), request.getAmount(), disbursementId);
        disbursement.setAmount(request.getAmount());
        disbursement.setDisbursedDate(request.getDisbursedDate());
        disbursement.setMethod(request.getMethod());
        disbursement.setReference(request.getReference());
        return toDisbursementResponse(loanDisbursementRepository.save(disbursement));
    }

    @Override
    public void deleteDisbursement(Long id, Long disbursementId) {
        LoanDisbursement disbursement = findDisbursementOrThrow(id, disbursementId);
        if (disbursement.getStatus() != DisbursementStatus.PENDING_APPROVAL) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING_APPROVAL disbursements can be deleted");
        }
        loanDisbursementRepository.delete(disbursement);
    }

    @Override
    @Transactional
    public LoanDisbursementResponse approveDisbursement(Long id, Long disbursementId) {
        LoanDisbursement disbursement = findDisbursementOrThrow(id, disbursementId);
        if (disbursement.getStatus() != DisbursementStatus.PENDING_APPROVAL) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING_APPROVAL disbursements can be approved");
        }
        assertDifferentFromCreator(disbursement, "approve");
        assertWithinPrincipal(disbursement.getLoan(), disbursement.getAmount(), disbursementId);

        disbursement.setStatus(DisbursementStatus.APPROVED);
        disbursement.setReviewedBy(currentUsername());
        disbursement.setReviewedAt(LocalDateTime.now());
        LoanDisbursement saved = loanDisbursementRepository.save(disbursement);

        Long journalEntryId = recordTransaction(saved.getLoan(), TransactionType.DISBURSEMENT, saved.getAmount(),
                saved.getDisbursedDate(), "LoanDisbursement", saved.getId(), saved.getReference());
        if (journalEntryId != null) {
            // Remembered so voidDisbursement can reverse this specific entry later.
            saved.setJournalEntryId(journalEntryId);
            saved = loanDisbursementRepository.save(saved);
        }
        return toDisbursementResponse(saved);
    }

    @Override
    public LoanDisbursementResponse rejectDisbursement(Long id, Long disbursementId, DisbursementReasonRequest request) {
        LoanDisbursement disbursement = findDisbursementOrThrow(id, disbursementId);
        if (disbursement.getStatus() != DisbursementStatus.PENDING_APPROVAL) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING_APPROVAL disbursements can be rejected");
        }
        assertDifferentFromCreator(disbursement, "reject");

        disbursement.setStatus(DisbursementStatus.REJECTED);
        disbursement.setReviewedBy(currentUsername());
        disbursement.setReviewedAt(LocalDateTime.now());
        disbursement.setRejectionReason(request.getReason());
        return toDisbursementResponse(loanDisbursementRepository.save(disbursement));
    }

    @Override
    @Transactional
    public LoanDisbursementResponse voidDisbursement(Long id, Long disbursementId, DisbursementReasonRequest request) {
        LoanDisbursement disbursement = findDisbursementOrThrow(id, disbursementId);
        if (disbursement.getStatus() != DisbursementStatus.APPROVED) {
            throw new AppException(HttpStatus.CONFLICT, "Only APPROVED disbursements can be voided");
        }

        disbursement.setStatus(DisbursementStatus.VOIDED);
        disbursement.setVoidedBy(currentUsername());
        disbursement.setVoidedAt(LocalDateTime.now());
        disbursement.setVoidReason(request.getReason());
        LoanDisbursement saved = loanDisbursementRepository.save(disbursement);

        recordTransaction(saved.getLoan(), TransactionType.ADJUSTMENT, saved.getAmount().negate(), LocalDate.now(),
                "LoanDisbursement", saved.getId(), "Void: " + request.getReason());
        if (saved.getJournalEntryId() != null) {
            // Reverses the specific entry approveDisbursement generated, rather than relying
            // on the local ADJUSTMENT above (which has no accounting-service equivalent).
            accountingClient.reverse(saved.getJournalEntryId());
        }
        return toDisbursementResponse(saved);
    }

    private LoanDisbursement findDisbursementOrThrow(Long loanId, Long disbursementId) {
        LoanDisbursement disbursement = loanDisbursementRepository.findById(disbursementId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan disbursement", disbursementId));
        if (!disbursement.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan disbursement", disbursementId);
        }
        return disbursement;
    }

    private void assertDifferentFromCreator(LoanDisbursement disbursement, String action) {
        if (currentUsername().equals(disbursement.getCreatedBy())) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot " + action + " a disbursement you created");
        }
    }

    private void assertWithinPrincipal(Loan loan, BigDecimal amount, Long excludingDisbursementId) {
        BigDecimal committed = loanDisbursementRepository.findByLoanIdOrderByDisbursedDateAsc(loan.getId()).stream()
                .filter(d -> !d.getId().equals(excludingDisbursementId))
                .filter(d -> d.getStatus() == DisbursementStatus.PENDING_APPROVAL || d.getStatus() == DisbursementStatus.APPROVED)
                .map(LoanDisbursement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (committed.add(amount).compareTo(loan.getPrincipal()) > 0) {
            throw new AppException(HttpStatus.CONFLICT, "Disbursement exceeds loan principal");
        }
    }

    @Override
    public LoanGuarantorResponse addGuarantor(Long id, LoanGuarantorRequest request) {
        Loan loan = findOrThrow(id);
        LoanGuarantor guarantor = LoanGuarantor.builder()
                .loan(loan)
                .name(request.getName())
                .phone(request.getPhone())
                .relationship(request.getRelationship())
                .guaranteedAmount(request.getGuaranteedAmount())
                .status(GuarantorStatus.ACTIVE)
                .build();
        return toGuarantorResponse(loanGuarantorRepository.save(guarantor));
    }

    @Override
    public List<LoanGuarantorResponse> getGuarantors(Long id) {
        findOrThrow(id);
        return loanGuarantorRepository.findByLoanIdOrderByCreatedAtAsc(id).stream()
                .map(this::toGuarantorResponse)
                .toList();
    }

    @Override
    public LoanGuarantorResponse updateGuarantor(Long id, Long guarantorId, LoanGuarantorRequest request) {
        LoanGuarantor guarantor = findGuarantorOrThrow(id, guarantorId);
        if (guarantor.getStatus() != GuarantorStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE guarantors can be edited");
        }
        guarantor.setName(request.getName());
        guarantor.setPhone(request.getPhone());
        guarantor.setRelationship(request.getRelationship());
        guarantor.setGuaranteedAmount(request.getGuaranteedAmount());
        return toGuarantorResponse(loanGuarantorRepository.save(guarantor));
    }

    @Override
    public void deleteGuarantor(Long id, Long guarantorId) {
        LoanGuarantor guarantor = findGuarantorOrThrow(id, guarantorId);
        if (guarantor.getStatus() != GuarantorStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE guarantors can be deleted");
        }
        loanGuarantorRepository.delete(guarantor);
    }

    @Override
    public LoanGuarantorResponse releaseGuarantor(Long id, Long guarantorId) {
        LoanGuarantor guarantor = findGuarantorOrThrow(id, guarantorId);
        if (guarantor.getStatus() != GuarantorStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE guarantors can be released");
        }
        guarantor.setStatus(GuarantorStatus.RELEASED);
        guarantor.setReleasedAt(LocalDateTime.now());
        return toGuarantorResponse(loanGuarantorRepository.save(guarantor));
    }

    @Override
    public LoanCollateralResponse addCollateral(Long id, LoanCollateralRequest request) {
        Loan loan = findOrThrow(id);
        LoanCollateral collateral = LoanCollateral.builder()
                .loan(loan)
                .type(request.getType())
                .description(request.getDescription())
                .estimatedValue(request.getEstimatedValue())
                .reference(request.getReference())
                .status(CollateralStatus.PLEDGED)
                .build();
        return toCollateralResponse(loanCollateralRepository.save(collateral));
    }

    @Override
    public List<LoanCollateralResponse> getCollaterals(Long id) {
        findOrThrow(id);
        return loanCollateralRepository.findByLoanIdOrderByCreatedAtAsc(id).stream()
                .map(this::toCollateralResponse)
                .toList();
    }

    @Override
    public LoanCollateralResponse updateCollateral(Long id, Long collateralId, LoanCollateralRequest request) {
        LoanCollateral collateral = findCollateralOrThrow(id, collateralId);
        if (collateral.getStatus() != CollateralStatus.PLEDGED) {
            throw new AppException(HttpStatus.CONFLICT, "Only PLEDGED collateral can be edited");
        }
        collateral.setType(request.getType());
        collateral.setDescription(request.getDescription());
        collateral.setEstimatedValue(request.getEstimatedValue());
        collateral.setReference(request.getReference());
        return toCollateralResponse(loanCollateralRepository.save(collateral));
    }

    @Override
    public void deleteCollateral(Long id, Long collateralId) {
        LoanCollateral collateral = findCollateralOrThrow(id, collateralId);
        if (collateral.getStatus() != CollateralStatus.PLEDGED) {
            throw new AppException(HttpStatus.CONFLICT, "Only PLEDGED collateral can be deleted");
        }
        loanCollateralRepository.delete(collateral);
    }

    @Override
    public LoanCollateralResponse releaseCollateral(Long id, Long collateralId) {
        LoanCollateral collateral = findCollateralOrThrow(id, collateralId);
        if (collateral.getStatus() != CollateralStatus.PLEDGED) {
            throw new AppException(HttpStatus.CONFLICT, "Only PLEDGED collateral can be released");
        }
        collateral.setStatus(CollateralStatus.RELEASED);
        collateral.setReleasedAt(LocalDateTime.now());
        return toCollateralResponse(loanCollateralRepository.save(collateral));
    }

    @Override
    public LoanDocumentResponse addDocument(Long id, LoanDocumentRequest request) {
        Loan loan = findOrThrow(id);
        LoanDocument document = LoanDocument.builder()
                .loan(loan)
                .name(request.getName())
                .status(request.getStatus())
                .notes(request.getNotes())
                .build();
        return toDocumentResponse(loanDocumentRepository.save(document));
    }

    @Override
    public List<LoanDocumentResponse> getDocuments(Long id) {
        findOrThrow(id);
        return loanDocumentRepository.findByLoanIdOrderByCreatedAtAsc(id).stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    @Override
    public LoanDocumentResponse updateDocumentStatus(Long id, Long documentId, LoanDocumentStatusUpdateRequest request) {
        LoanDocument document = findDocumentOrThrow(id, documentId);
        document.setStatus(request.getStatus());
        return toDocumentResponse(loanDocumentRepository.save(document));
    }

    @Override
    public void deleteDocument(Long id, Long documentId) {
        LoanDocument document = findDocumentOrThrow(id, documentId);
        loanDocumentRepository.delete(document);
    }

    @Override
    public LoanNoteResponse addNote(Long id, LoanNoteRequest request) {
        Loan loan = findOrThrow(id);
        LoanNote note = LoanNote.builder()
                .loan(loan)
                .authorName(currentUsername())
                .note(request.getNote())
                .build();
        return toNoteResponse(loanNoteRepository.save(note));
    }

    @Override
    public List<LoanNoteResponse> getNotes(Long id) {
        findOrThrow(id);
        return loanNoteRepository.findByLoanIdOrderByCreatedAtAsc(id).stream()
                .map(this::toNoteResponse)
                .toList();
    }

    @Override
    public List<LoanScheduleResponse> getSchedules(Long id) {
        findOrThrow(id);
        return loanScheduleRepository.findByLoanIdOrderByGeneratedAtDesc(id).stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Override
    public List<LoanScheduleInstallmentResponse> getScheduleInstallments(Long id, Long scheduleId) {
        findScheduleOrThrow(id, scheduleId);
        return loanScheduleInstallmentRepository.findByScheduleIdOrderByInstallmentNumberAsc(scheduleId).stream()
                .map(this::toScheduleInstallmentResponse)
                .toList();
    }

    @Override
    @Transactional
    public LoanPaymentResponse addPayment(Long id, LoanPaymentRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Payments can only be recorded for ACTIVE loans");
        }

        LoanPayment payment = LoanPayment.builder()
                .loan(loan)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .method(request.getMethod())
                .reference(request.getReference())
                .build();
        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        savedPayment.setPaymentNo(generatePaymentNo(savedPayment));
        savedPayment = loanPaymentRepository.save(savedPayment);

        // Waterfall: fees, then penalties (both paid in full or not at all), then whatever's
        // left goes against the schedule. Only the schedule portion reduces outstandingBalance
        // — see applyToOutstandingFeesAndPenalties for why.
        BigDecimal feesAndPenaltiesApplied =
                applyToOutstandingFeesAndPenalties(loan, request.getAmount(), request.getPaymentDate());
        BigDecimal remainingForSchedule = request.getAmount().subtract(feesAndPenaltiesApplied);
        List<LoanPaymentDetail> details = allocatePayment(loan, savedPayment, remainingForSchedule);

        BigDecimal newBalance = loan.getOutstandingBalance().subtract(remainingForSchedule);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        loan.setOutstandingBalance(newBalance);
        boolean closing = newBalance.compareTo(BigDecimal.ZERO) == 0;
        if (closing) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedAt(LocalDateTime.now());
        }
        Loan savedLoan = loanRepository.save(loan);
        if (closing) {
            recordStatusHistory(savedLoan, LoanStatus.ACTIVE, LoanStatus.CLOSED, null);
        }

        // Ledger entries are written after the balance is final so
        // balanceAfter on every row reflects the post-payment balance.
        BigDecimal totalAllocated = BigDecimal.ZERO;
        for (LoanPaymentDetail detail : details) {
            String description = "Installment #" + detail.getScheduleInstallment().getInstallmentNumber();
            if (detail.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0) {
                recordTransaction(savedLoan, TransactionType.PRINCIPAL_PAYMENT, detail.getPrincipalAmount(),
                        request.getPaymentDate(), "LoanPaymentDetail", detail.getId(), description);
            }
            if (detail.getInterestAmount().compareTo(BigDecimal.ZERO) > 0) {
                recordTransaction(savedLoan, TransactionType.INTEREST_PAYMENT, detail.getInterestAmount(),
                        request.getPaymentDate(), "LoanPaymentDetail", detail.getId(), description);
            }
            totalAllocated = totalAllocated.add(detail.getPrincipalAmount()).add(detail.getInterestAmount());
        }
        // No ACTIVE schedule, or the payment exceeds every unpaid installment
        // (e.g. an overpayment) — still record the balance reduction.
        BigDecimal unallocated = remainingForSchedule.subtract(totalAllocated);
        if (unallocated.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(savedLoan, TransactionType.PRINCIPAL_PAYMENT, unallocated,
                    request.getPaymentDate(), "LoanPayment", savedPayment.getId(), "Unallocated payment amount");
        }

        return toPaymentResponse(savedPayment);
    }

    @Override
    public List<LoanPaymentResponse> getPayments(Long id) {
        findOrThrow(id);
        return loanPaymentRepository.findByLoanIdOrderByPaymentDateAsc(id).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Override
    public LoanPayoffQuoteResponse getPayoffQuote(Long id) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Payoff quotes are only available for ACTIVE loans");
        }
        return computePayoffQuote(loan);
    }

    @Override
    @Transactional
    public LoanResponse payoff(Long id, LoanPayoffRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE loans can be paid off");
        }
        LoanPayoffQuoteResponse quote = computePayoffQuote(loan);
        LocalDate today = LocalDate.now();

        LoanPayment payment = LoanPayment.builder()
                .loan(loan)
                .amount(quote.getTotalPayoffAmount())
                .paymentDate(today)
                .method(request.getMethod())
                .reference(request.getReference() != null ? request.getReference() : "Early payoff")
                .build();
        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        savedPayment.setPaymentNo(generatePaymentNo(savedPayment));
        loanPaymentRepository.save(savedPayment);

        // Pays every currently-PENDING fee/penalty in full — quote.outstandingFees/
        // outstandingPenalties was computed from exactly the same PENDING rows a moment ago
        // in this same transaction, so the amount-limited waterfall consumes them completely
        // with nothing left over.
        BigDecimal feesAndPenaltiesTotal = quote.getOutstandingFees().add(quote.getOutstandingPenalties());
        if (feesAndPenaltiesTotal.compareTo(BigDecimal.ZERO) > 0) {
            applyToOutstandingFeesAndPenalties(loan, feesAndPenaltiesTotal, today);
        }

        // The remaining schedule installments are moot once the loan is closing early — the
        // payoff is one aggregate settlement of principal + accrued interest, not an
        // installment-by-installment allocation (their original amounts assumed the loan ran
        // to full term), so the schedule is superseded rather than allocatePayment'd through.
        loanScheduleRepository.findByLoanIdAndStatus(id, ScheduleStatus.ACTIVE)
                .forEach(schedule -> {
                    schedule.setStatus(ScheduleStatus.SUPERSEDED);
                    loanScheduleRepository.save(schedule);
                });

        LoanStatus previousStatus = loan.getStatus();
        loan.setOutstandingBalance(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedAt(LocalDateTime.now());
        Loan saved = loanRepository.save(loan);
        recordStatusHistory(saved, previousStatus, LoanStatus.CLOSED, "Paid off early");

        if (quote.getRemainingPrincipal().compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(saved, TransactionType.PRINCIPAL_PAYMENT, quote.getRemainingPrincipal(), today,
                    "LoanPayment", savedPayment.getId(), "Early payoff — remaining principal");
        }
        if (quote.getAccruedInterest().compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(saved, TransactionType.INTEREST_PAYMENT, quote.getAccruedInterest(), today,
                    "LoanPayment", savedPayment.getId(), "Early payoff — accrued interest");
        }

        CustomerResponse customer = customerClient.getById(saved.getCustomerId()).getData();
        return toResponse(saved, customer);
    }

    // Simple daily-interest accrual on the remaining principal since the last payment (or
    // since disbursement if there hasn't been one) — deliberately not Rule-of-78 or any other
    // front-loaded method, which overcharges interest relative to time actually elapsed.
    // Loan.outstandingBalance is NOT used here: it's set at disbursement to the sum of every
    // future installment (principal and interest for the full term — see disburse()) and
    // never discounted for interest not yet accrued, so it overstates what an early payoff
    // should actually cost.
    private LoanPayoffQuoteResponse computePayoffQuote(Loan loan) {
        BigDecimal principalPaid = loanPaymentDetailRepository.findByPayment_LoanId(loan.getId()).stream()
                .map(LoanPaymentDetail::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingPrincipal = loan.getPrincipal().subtract(principalPaid).max(BigDecimal.ZERO);

        LocalDate accrualStart = loanPaymentRepository.findByLoanIdOrderByPaymentDateAsc(loan.getId()).stream()
                .map(LoanPayment::getPaymentDate)
                .max(LocalDate::compareTo)
                .orElseGet(() -> loan.getDisbursedAt().toLocalDate());
        LocalDate asOf = LocalDate.now();
        long daysAccrued = Math.max(0, ChronoUnit.DAYS.between(accrualStart, asOf));

        BigDecimal dailyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        BigDecimal accruedInterest = remainingPrincipal.multiply(dailyRate).multiply(BigDecimal.valueOf(daysAccrued))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal outstandingFees = loanFeeRepository.findByLoanIdOrderByChargedDateAsc(loan.getId()).stream()
                .filter(fee -> fee.getStatus() == FeeStatus.PENDING)
                .map(LoanFee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingPenalties = loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(loan.getId()).stream()
                .filter(penalty -> penalty.getStatus() == PenaltyStatus.PENDING)
                .map(LoanPenalty::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = remainingPrincipal.add(accruedInterest).add(outstandingFees).add(outstandingPenalties);

        return LoanPayoffQuoteResponse.builder()
                .loanId(loan.getId())
                .asOfDate(asOf)
                .remainingPrincipal(remainingPrincipal)
                .accruedInterest(accruedInterest)
                .outstandingFees(outstandingFees)
                .outstandingPenalties(outstandingPenalties)
                .totalPayoffAmount(total)
                .build();
    }

    @Override
    public LoanInterestResponse addInterestAccrual(Long id, LoanInterestRequest request) {
        Loan loan = findOrThrow(id);
        LoanInterestAccrual accrual = LoanInterestAccrual.builder()
                .loan(loan)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .rate(request.getRate())
                .amount(request.getAmount())
                .accruedAt(LocalDateTime.now())
                .build();
        return toInterestResponse(loanInterestAccrualRepository.save(accrual));
    }

    @Override
    public List<LoanInterestResponse> getInterestAccruals(Long id) {
        findOrThrow(id);
        return loanInterestAccrualRepository.findByLoanIdOrderByPeriodStartAsc(id).stream()
                .map(this::toInterestResponse)
                .toList();
    }

    @Override
    public LoanInterestResponse updateInterestAccrual(Long id, Long accrualId, LoanInterestRequest request) {
        LoanInterestAccrual accrual = findInterestAccrualOrThrow(id, accrualId);
        accrual.setPeriodStart(request.getPeriodStart());
        accrual.setPeriodEnd(request.getPeriodEnd());
        accrual.setRate(request.getRate());
        accrual.setAmount(request.getAmount());
        return toInterestResponse(loanInterestAccrualRepository.save(accrual));
    }

    @Override
    public void deleteInterestAccrual(Long id, Long accrualId) {
        LoanInterestAccrual accrual = findInterestAccrualOrThrow(id, accrualId);
        loanInterestAccrualRepository.delete(accrual);
    }

    @Override
    public PageResponse<LoanInterestResponse> getAllInterestAccruals(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanInterestAccrualRepository.findAll(pageable).map(this::toInterestResponse));
    }

    @Override
    public LoanPenaltyResponse addPenalty(Long id, LoanPenaltyRequest request) {
        Loan loan = findOrThrow(id);
        LoanPenalty penalty = LoanPenalty.builder()
                .loan(loan)
                .amount(request.getAmount())
                .reason(request.getReason())
                .appliedDate(request.getAppliedDate())
                .status(PenaltyStatus.PENDING)
                .build();
        return toPenaltyResponse(loanPenaltyRepository.save(penalty));
    }

    @Override
    public List<LoanPenaltyResponse> getPenalties(Long id) {
        findOrThrow(id);
        return loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(id).stream()
                .map(this::toPenaltyResponse)
                .toList();
    }

    @Override
    public LoanPenaltyResponse updatePenalty(Long id, Long penaltyId, LoanPenaltyRequest request) {
        LoanPenalty penalty = findPenaltyOrThrow(id, penaltyId);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING penalties can be edited");
        }
        penalty.setAmount(request.getAmount());
        penalty.setReason(request.getReason());
        penalty.setAppliedDate(request.getAppliedDate());
        return toPenaltyResponse(loanPenaltyRepository.save(penalty));
    }

    @Override
    public void deletePenalty(Long id, Long penaltyId) {
        LoanPenalty penalty = findPenaltyOrThrow(id, penaltyId);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING penalties can be deleted");
        }
        loanPenaltyRepository.delete(penalty);
    }

    @Override
    @Transactional
    public LoanPenaltyResponse payPenalty(Long id, Long penaltyId) {
        Loan loan = findOrThrow(id);
        LoanPenalty penalty = findPenaltyOrThrow(id, penaltyId);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING penalties can be marked paid");
        }
        penalty.setStatus(PenaltyStatus.PAID);
        penalty.setPaidAt(LocalDateTime.now());
        LoanPenalty saved = loanPenaltyRepository.save(penalty);
        recordTransaction(loan, TransactionType.PENALTY_PAYMENT, saved.getAmount(), LocalDate.now(),
                "LoanPenalty", saved.getId(), saved.getReason());
        return toPenaltyResponse(saved);
    }

    @Override
    public LoanPenaltyResponse waivePenalty(Long id, Long penaltyId) {
        LoanPenalty penalty = findPenaltyOrThrow(id, penaltyId);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING penalties can be waived");
        }
        penalty.setStatus(PenaltyStatus.WAIVED);
        penalty.setWaivedAt(LocalDateTime.now());
        return toPenaltyResponse(loanPenaltyRepository.save(penalty));
    }

    @Override
    public PageResponse<LoanPenaltyResponse> getAllPenalties(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanPenaltyRepository.findAll(pageable).map(this::toPenaltyResponse));
    }

    @Override
    public LoanFeeResponse addFee(Long id, LoanFeeRequest request) {
        Loan loan = findOrThrow(id);
        LoanFee fee = LoanFee.builder()
                .loan(loan)
                .type(request.getType())
                .amount(request.getAmount())
                .chargedDate(request.getChargedDate())
                .description(request.getDescription())
                .status(FeeStatus.PENDING)
                .build();
        return toFeeResponse(loanFeeRepository.save(fee));
    }

    @Override
    public List<LoanFeeResponse> getFees(Long id) {
        findOrThrow(id);
        return loanFeeRepository.findByLoanIdOrderByChargedDateAsc(id).stream()
                .map(this::toFeeResponse)
                .toList();
    }

    @Override
    public LoanFeeResponse updateFee(Long id, Long feeId, LoanFeeRequest request) {
        LoanFee fee = findFeeOrThrow(id, feeId);
        if (fee.getStatus() != FeeStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING fees can be edited");
        }
        fee.setType(request.getType());
        fee.setAmount(request.getAmount());
        fee.setChargedDate(request.getChargedDate());
        fee.setDescription(request.getDescription());
        return toFeeResponse(loanFeeRepository.save(fee));
    }

    @Override
    public void deleteFee(Long id, Long feeId) {
        LoanFee fee = findFeeOrThrow(id, feeId);
        if (fee.getStatus() != FeeStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING fees can be deleted");
        }
        loanFeeRepository.delete(fee);
    }

    @Override
    @Transactional
    public LoanFeeResponse payFee(Long id, Long feeId) {
        Loan loan = findOrThrow(id);
        LoanFee fee = findFeeOrThrow(id, feeId);
        if (fee.getStatus() != FeeStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING fees can be marked paid");
        }
        fee.setStatus(FeeStatus.PAID);
        fee.setPaidAt(LocalDateTime.now());
        LoanFee saved = loanFeeRepository.save(fee);
        recordTransaction(loan, TransactionType.FEE_PAYMENT, saved.getAmount(), LocalDate.now(),
                "LoanFee", saved.getId(), saved.getDescription());
        return toFeeResponse(saved);
    }

    @Override
    public LoanFeeResponse waiveFee(Long id, Long feeId) {
        LoanFee fee = findFeeOrThrow(id, feeId);
        if (fee.getStatus() != FeeStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only PENDING fees can be waived");
        }
        fee.setStatus(FeeStatus.WAIVED);
        fee.setWaivedAt(LocalDateTime.now());
        return toFeeResponse(loanFeeRepository.save(fee));
    }

    @Override
    @Transactional
    public LoanRestructureResponse addRestructure(Long id, LoanRestructureRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE loans can be restructured");
        }

        // Re-amortize what's actually left, not the original principal.
        BigDecimal outstanding = loan.getOutstandingBalance();
        BigDecimal newRate = request.getNewInterestRate() != null ? request.getNewInterestRate() : loan.getInterestRate();

        List<AmortizationCalculator.Installment> schedule = AmortizationCalculator.generateSchedule(
                outstanding, newRate, request.getNewTermMonths(), request.getEffectiveDate());
        BigDecimal emi = AmortizationCalculator.calculateEmi(outstanding, newRate, request.getNewTermMonths());

        loan.setTermMonths(request.getNewTermMonths());
        loan.setInterestRate(newRate);
        loan.setMaturityDate(request.getEffectiveDate().plusMonths(request.getNewTermMonths()));
        loan.setMonthlyInstallment(emi);
        Loan savedLoan = loanRepository.save(loan);

        generateAndPersistSchedule(savedLoan, schedule, outstanding);

        LoanRestructure restructure = LoanRestructure.builder()
                .loan(savedLoan)
                .newTermMonths(request.getNewTermMonths())
                .newInterestRate(request.getNewInterestRate())
                .reason(request.getReason())
                .effectiveDate(request.getEffectiveDate())
                .build();
        return toRestructureResponse(loanRestructureRepository.save(restructure));
    }

    @Override
    public List<LoanRestructureResponse> getRestructures(Long id) {
        findOrThrow(id);
        return loanRestructureRepository.findByLoanIdOrderByEffectiveDateAsc(id).stream()
                .map(this::toRestructureResponse)
                .toList();
    }

    @Override
    public PageResponse<LoanRestructureResponse> getAllRestructures(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanRestructureRepository.findAll(pageable).map(this::toRestructureResponse));
    }

    @Override
    @Transactional
    public LoanRefinanceResponse addRefinance(Long id, LoanRefinanceRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE loans can be refinanced");
        }
        if (request.getNewLoanId().equals(id)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A loan cannot be refinanced into itself");
        }
        Loan newLoan = findOrThrow(request.getNewLoanId());
        if (!newLoan.getCustomerId().equals(loan.getCustomerId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "The replacement loan must belong to the same customer");
        }

        LoanRefinance refinance = LoanRefinance.builder()
                .loan(loan)
                .newLoanId(request.getNewLoanId())
                .reason(request.getReason())
                .effectiveDate(request.getEffectiveDate())
                .build();
        LoanRefinance savedRefinance = loanRefinanceRepository.save(refinance);

        // Previously this only wrote the note above and left the old loan ACTIVE with its
        // own outstanding balance forever — both loans stayed on the books simultaneously,
        // double-counting in portfolio totals (including the GL reconciliation check).
        // Refinancing means this balance is paid off by the new loan, not written off or
        // forgiven, but there's no cash transaction backing it either — booked as a
        // loan-service-only ADJUSTMENT (same treatment as voidDisbursement's local entry)
        // rather than inventing an accounting-service TransactionType with no sign-off.
        BigDecimal payoffAmount = loan.getOutstandingBalance();
        LoanStatus previousStatus = loan.getStatus();
        loan.setOutstandingBalance(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedAt(LocalDateTime.now());
        Loan savedLoan = loanRepository.save(loan);
        recordStatusHistory(savedLoan, previousStatus, LoanStatus.CLOSED,
                "Refinanced into loan #" + request.getNewLoanId());
        if (payoffAmount.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(savedLoan, TransactionType.ADJUSTMENT, payoffAmount.negate(), request.getEffectiveDate(),
                    "LoanRefinance", savedRefinance.getId(), "Payoff via refinance into loan #" + request.getNewLoanId());
        }

        return toRefinanceResponse(savedRefinance);
    }

    @Override
    public List<LoanRefinanceResponse> getRefinances(Long id) {
        findOrThrow(id);
        return loanRefinanceRepository.findByLoanIdOrderByEffectiveDateAsc(id).stream()
                .map(this::toRefinanceResponse)
                .toList();
    }

    @Override
    public PageResponse<LoanRefinanceResponse> getAllRefinances(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanRefinanceRepository.findAll(pageable).map(this::toRefinanceResponse));
    }

    @Override
    public LoanSettlementResponse addSettlement(Long id, LoanSettlementRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE loans can be settled");
        }
        if (loanSettlementRepository.findByLoanId(id).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "A settlement has already been recorded for this loan");
        }
        if (loanWriteoffRepository.findByLoanId(id).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "This loan has already been written off");
        }

        LoanSettlement settlement = LoanSettlement.builder()
                .loan(loan)
                .settlementAmount(request.getSettlementAmount())
                .settlementDate(request.getSettlementDate())
                .note(request.getNote())
                .status(SettlementStatus.PENDING)
                .build();
        return toSettlementResponse(loanSettlementRepository.save(settlement));
    }

    @Override
    public LoanSettlementResponse getSettlement(Long id) {
        findOrThrow(id);
        return loanSettlementRepository.findByLoanId(id)
                .map(this::toSettlementResponse)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No settlement recorded for this loan"));
    }

    @Override
    @Transactional
    public LoanSettlementResponse completeSettlement(Long id) {
        Loan loan = findOrThrow(id);
        LoanSettlement settlement = loanSettlementRepository.findByLoanId(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No settlement recorded for this loan"));
        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only a PENDING settlement can be completed");
        }

        settlement.setStatus(SettlementStatus.COMPLETED);
        LoanSettlement savedSettlement = loanSettlementRepository.save(settlement);

        Loan closedLoan = closeLoan(loan);
        recordTransaction(closedLoan, TransactionType.SETTLEMENT, savedSettlement.getSettlementAmount(),
                savedSettlement.getSettlementDate(), "LoanSettlement", savedSettlement.getId(), savedSettlement.getNote());

        return toSettlementResponse(savedSettlement);
    }

    @Override
    public LoanWriteoffResponse addWriteoff(Long id, LoanWriteoffRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Only ACTIVE loans can be written off");
        }
        if (loanWriteoffRepository.findByLoanId(id).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "A write-off has already been recorded for this loan");
        }
        if (loanSettlementRepository.findByLoanId(id).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "This loan has already been settled");
        }

        LoanWriteoff writeoff = LoanWriteoff.builder()
                .loan(loan)
                .amount(request.getAmount())
                .reason(request.getReason())
                .writeoffDate(request.getWriteoffDate())
                .status(WriteoffStatus.PENDING)
                .build();
        return toWriteoffResponse(loanWriteoffRepository.save(writeoff));
    }

    @Override
    public LoanWriteoffResponse getWriteoff(Long id) {
        findOrThrow(id);
        return loanWriteoffRepository.findByLoanId(id)
                .map(this::toWriteoffResponse)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No write-off recorded for this loan"));
    }

    @Override
    @Transactional
    public LoanWriteoffResponse completeWriteoff(Long id) {
        Loan loan = findOrThrow(id);
        LoanWriteoff writeoff = loanWriteoffRepository.findByLoanId(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No write-off recorded for this loan"));
        if (writeoff.getStatus() != WriteoffStatus.PENDING) {
            throw new AppException(HttpStatus.CONFLICT, "Only a PENDING write-off can be completed");
        }

        writeoff.setStatus(WriteoffStatus.COMPLETED);
        LoanWriteoff savedWriteoff = loanWriteoffRepository.save(writeoff);

        Loan closedLoan = closeLoan(loan);
        recordTransaction(closedLoan, TransactionType.WRITE_OFF, savedWriteoff.getAmount(),
                savedWriteoff.getWriteoffDate(), "LoanWriteoff", savedWriteoff.getId(), savedWriteoff.getReason());

        return toWriteoffResponse(savedWriteoff);
    }

    @Override
    public PageResponse<LoanWriteoffResponse> getAllWriteoffs(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanWriteoffRepository.findAll(pageable).map(this::toWriteoffResponse));
    }

    @Override
    @Transactional
    public LoanWriteoffRecoveryResponse recordWriteoffRecovery(Long id, LoanWriteoffRecoveryRequest request) {
        Loan loan = findOrThrow(id);
        LoanWriteoff writeoff = loanWriteoffRepository.findByLoanId(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No write-off recorded for this loan"));
        if (writeoff.getStatus() != WriteoffStatus.COMPLETED) {
            throw new AppException(HttpStatus.CONFLICT, "Recoveries can only be recorded against a COMPLETED write-off");
        }

        BigDecimal alreadyRecovered = loanWriteoffRecoveryRepository.findByWriteoffIdOrderByRecoveryDateAsc(writeoff.getId())
                .stream()
                .map(LoanWriteoffRecovery::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (alreadyRecovered.add(request.getAmount()).compareTo(writeoff.getAmount()) > 0) {
            throw new AppException(HttpStatus.BAD_REQUEST,
                    "Recovery of " + request.getAmount() + " would exceed the written-off amount of "
                            + writeoff.getAmount() + " (" + alreadyRecovered + " already recovered)");
        }

        LoanWriteoffRecovery recovery = LoanWriteoffRecovery.builder()
                .writeoff(writeoff)
                .amount(request.getAmount())
                .recoveryDate(request.getRecoveryDate())
                .method(request.getMethod())
                .reference(request.getReference())
                .createdBy(currentUsername())
                .build();
        LoanWriteoffRecovery saved = loanWriteoffRecoveryRepository.save(recovery);

        // The loan stays CLOSED — recovering a written-off debt doesn't reopen it. Recognized
        // as RECOVERY income (see JournalTemplateSeeder's "Bad Debt Recovery" template), not a
        // reversal of the original LOAN_WRITE_OFF entry, which stays posted as the historical
        // record of the charge-off.
        recordTransaction(loan, TransactionType.RECOVERY, saved.getAmount(), saved.getRecoveryDate(),
                "LoanWriteoffRecovery", saved.getId(), "Recovery on write-off #" + writeoff.getId());

        return toWriteoffRecoveryResponse(saved);
    }

    @Override
    public List<LoanWriteoffRecoveryResponse> getWriteoffRecoveries(Long id) {
        findOrThrow(id);
        LoanWriteoff writeoff = loanWriteoffRepository.findByLoanId(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No write-off recorded for this loan"));
        return loanWriteoffRecoveryRepository.findByWriteoffIdOrderByRecoveryDateAsc(writeoff.getId()).stream()
                .map(this::toWriteoffRecoveryResponse)
                .toList();
    }

    private LoanWriteoffRecoveryResponse toWriteoffRecoveryResponse(LoanWriteoffRecovery recovery) {
        return LoanWriteoffRecoveryResponse.builder()
                .id(recovery.getId())
                .writeoffId(recovery.getWriteoff().getId())
                .loanId(recovery.getWriteoff().getLoan().getId())
                .amount(recovery.getAmount())
                .recoveryDate(recovery.getRecoveryDate())
                .method(recovery.getMethod())
                .reference(recovery.getReference())
                .createdAt(recovery.getCreatedAt())
                .build();
    }

    // Shared by settlement/write-off completion: zeroes the balance and
    // closes the loan, regardless of settlementAmount/writeoff amount — a
    // negotiated or uncollectable payoff still means nothing more is owed.
    private Loan closeLoan(Loan loan) {
        LoanStatus previousStatus = loan.getStatus();
        loan.setOutstandingBalance(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedAt(LocalDateTime.now());
        Loan savedLoan = loanRepository.save(loan);
        recordStatusHistory(savedLoan, previousStatus, LoanStatus.CLOSED, null);
        return savedLoan;
    }

    @Override
    @Transactional
    public LoanAdjustmentResponse addAdjustment(Long id, LoanAdjustmentRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Adjustments can only be applied to ACTIVE loans");
        }

        LoanAdjustment adjustment = LoanAdjustment.builder()
                .loan(loan)
                .type(request.getType())
                .amount(request.getAmount())
                .reason(request.getReason())
                .build();
        LoanAdjustment savedAdjustment = loanAdjustmentRepository.save(adjustment);

        BigDecimal delta = request.getType() == AdjustmentType.CREDIT ? request.getAmount().negate() : request.getAmount();
        BigDecimal newBalance = loan.getOutstandingBalance().add(delta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }
        loan.setOutstandingBalance(newBalance);
        boolean closing = newBalance.compareTo(BigDecimal.ZERO) == 0;
        if (closing) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedAt(LocalDateTime.now());
        }
        Loan savedLoan = loanRepository.save(loan);
        if (closing) {
            recordStatusHistory(savedLoan, LoanStatus.ACTIVE, LoanStatus.CLOSED, null);
        }

        recordTransaction(savedLoan, TransactionType.ADJUSTMENT, request.getAmount(), LocalDate.now(),
                "LoanAdjustment", savedAdjustment.getId(), savedAdjustment.getReason());

        return toAdjustmentResponse(savedAdjustment);
    }

    @Override
    public List<LoanAdjustmentResponse> getAdjustments(Long id) {
        findOrThrow(id);
        return loanAdjustmentRepository.findByLoanIdOrderByCreatedAtAsc(id).stream()
                .map(this::toAdjustmentResponse)
                .toList();
    }

    @Override
    public List<LoanTransactionResponse> getTransactions(Long id) {
        findOrThrow(id);
        return loanTransactionRepository.findByLoanIdOrderByTransactionDateAscIdAsc(id).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    // Appends one row to the unified money-movement ledger. Called after the
    // loan's outstandingBalance has already been saved for whatever action
    // triggered it, so balanceAfter is always the real post-event balance.
    // Returns the accounting-service JournalEntry id created for this transaction, or null
    // if the type has no accounting equivalent (ADJUSTMENT/SETTLEMENT) — callers that need
    // to reverse the posting later (e.g. voidDisbursement) persist this id themselves.
    private Long recordTransaction(Loan loan, TransactionType type, BigDecimal amount, LocalDate transactionDate,
                                    String referenceType, Long referenceId, String description) {
        LoanTransaction transaction = LoanTransaction.builder()
                .loan(loan)
                .type(type)
                .amount(amount)
                .transactionDate(transactionDate)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .balanceAfter(loan.getOutstandingBalance())
                .build();
        transaction = loanTransactionRepository.save(transaction);

        String accountingTransactionType = toAccountingTransactionType(type);
        if (accountingTransactionType == null) {
            // ADJUSTMENT/SETTLEMENT have no accounting-service TransactionType equivalent
            // yet — left as loan-service-only ledger entries until one is defined.
            return null;
        }
        // referenceType/referenceId sent to accounting-service always point at this
        // LoanTransaction row rather than passing through the caller's own reference
        // (LoanPenalty, LoanFee, ...): it's guaranteed unique per call — required for
        // accounting-service's idempotency check on (transactionType, referenceType,
        // referenceId), since e.g. every principal payment on a loan would otherwise share
        // the same "Loan"/loanId pair and collide. It also carries the loan link (via
        // LoanTransaction.loan) and the original sub-entity reference in one hop, so nothing
        // is lost — see JournalEntryServiceImpl.generate's idempotency check.
        JournalEntryResponse response = accountingClient.generate(JournalEntryGenerateRequest.builder()
                .transactionType(accountingTransactionType)
                .transactionDate(transactionDate)
                .branchId(loan.getBranchId())
                .referenceType("LoanTransaction")
                .referenceId(transaction.getId().toString())
                .amount(amount)
                .description(description)
                .build()).getData();
        return response != null ? response.getId() : null;
    }

    // Maps loan-service's TransactionType to accounting-service's — the two enums don't
    // share names for every case (PENALTY_PAYMENT/PENALTY_CHARGE, FEE_PAYMENT/FEE_CHARGE,
    // WRITE_OFF/LOAN_WRITE_OFF), and ADJUSTMENT/SETTLEMENT have no equivalent at all.
    private String toAccountingTransactionType(TransactionType type) {
        return switch (type) {
            case DISBURSEMENT -> "DISBURSEMENT";
            case PRINCIPAL_PAYMENT -> "PRINCIPAL_PAYMENT";
            case INTEREST_PAYMENT -> "INTEREST_PAYMENT";
            case PENALTY_PAYMENT -> "PENALTY_CHARGE";
            case FEE_PAYMENT -> "FEE_CHARGE";
            case WRITE_OFF -> "LOAN_WRITE_OFF";
            case RECOVERY -> "RECOVERY";
            case ADJUSTMENT, SETTLEMENT -> null;
        };
    }

    private LoanAdjustmentResponse toAdjustmentResponse(LoanAdjustment adjustment) {
        return LoanAdjustmentResponse.builder()
                .id(adjustment.getId())
                .loanId(adjustment.getLoan().getId())
                .type(adjustment.getType())
                .amount(adjustment.getAmount())
                .reason(adjustment.getReason())
                .createdAt(adjustment.getCreatedAt())
                .updatedAt(adjustment.getUpdatedAt())
                .build();
    }

    private LoanTransactionResponse toTransactionResponse(LoanTransaction transaction) {
        return LoanTransactionResponse.builder()
                .id(transaction.getId())
                .loanId(transaction.getLoan().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    // First stage of the payment waterfall: oldest pending fee first, then oldest pending
    // penalty, each paid in full or not at all (LoanFee/LoanPenalty have no partial-paid
    // status — unlike schedule installments, which support PARTIALLY_PAID). Returns how
    // much of paymentAmount this consumed, so the caller passes only the remainder into
    // allocatePayment/outstandingBalance: fees and penalties are tracked separately from
    // Loan.outstandingBalance (see payFee/payPenalty, neither of which touches it), so a
    // payment that goes toward a fee must not also be subtracted from the principal/interest
    // balance, or the loan would look more paid down than it actually is.
    private BigDecimal applyToOutstandingFeesAndPenalties(Loan loan, BigDecimal paymentAmount, LocalDate paymentDate) {
        BigDecimal remaining = paymentAmount;

        List<LoanFee> pendingFees = loanFeeRepository.findByLoanIdOrderByChargedDateAsc(loan.getId()).stream()
                .filter(fee -> fee.getStatus() == FeeStatus.PENDING)
                .toList();
        for (LoanFee fee : pendingFees) {
            if (remaining.compareTo(fee.getAmount()) < 0) {
                break;
            }
            fee.setStatus(FeeStatus.PAID);
            fee.setPaidAt(LocalDateTime.now());
            LoanFee saved = loanFeeRepository.save(fee);
            recordTransaction(loan, TransactionType.FEE_PAYMENT, saved.getAmount(), paymentDate,
                    "LoanFee", saved.getId(), saved.getDescription());
            remaining = remaining.subtract(saved.getAmount());
        }

        List<LoanPenalty> pendingPenalties = loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(loan.getId()).stream()
                .filter(penalty -> penalty.getStatus() == PenaltyStatus.PENDING)
                .toList();
        for (LoanPenalty penalty : pendingPenalties) {
            if (remaining.compareTo(penalty.getAmount()) < 0) {
                break;
            }
            penalty.setStatus(PenaltyStatus.PAID);
            penalty.setPaidAt(LocalDateTime.now());
            LoanPenalty saved = loanPenaltyRepository.save(penalty);
            recordTransaction(loan, TransactionType.PENALTY_PAYMENT, saved.getAmount(), paymentDate,
                    "LoanPenalty", saved.getId(), saved.getReason());
            remaining = remaining.subtract(saved.getAmount());
        }

        return paymentAmount.subtract(remaining);
    }

    // Second stage of the waterfall, against the loan's ACTIVE schedule: oldest unpaid
    // installment first, interest before principal within each installment. No-ops (payment
    // recorded, nothing allocated) if the loan has no ACTIVE schedule yet.
    private List<LoanPaymentDetail> allocatePayment(Loan loan, LoanPayment payment, BigDecimal paymentAmount) {
        List<LoanPaymentDetail> createdDetails = new ArrayList<>();
        List<LoanSchedule> activeSchedules = loanScheduleRepository.findByLoanIdAndStatus(loan.getId(), ScheduleStatus.ACTIVE);
        if (activeSchedules.isEmpty()) {
            return createdDetails;
        }
        LoanSchedule activeSchedule = activeSchedules.get(0);

        List<LoanScheduleInstallment> unpaidInstallments = loanScheduleInstallmentRepository
                .findByScheduleIdAndStatusNotOrderByInstallmentNumberAsc(activeSchedule.getId(), ScheduleInstallmentStatus.PAID);

        BigDecimal remaining = paymentAmount;
        for (LoanScheduleInstallment installment : unpaidInstallments) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            List<LoanPaymentDetail> existingDetails = loanPaymentDetailRepository.findByScheduleInstallmentId(installment.getId());
            BigDecimal interestPaidSoFar = existingDetails.stream()
                    .map(LoanPaymentDetail::getInterestAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal principalPaidSoFar = existingDetails.stream()
                    .map(LoanPaymentDetail::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal interestRemaining = installment.getInterestAmount().subtract(interestPaidSoFar).max(BigDecimal.ZERO);
            BigDecimal principalRemaining = installment.getPrincipalAmount().subtract(principalPaidSoFar).max(BigDecimal.ZERO);
            BigDecimal installmentRemaining = interestRemaining.add(principalRemaining);
            if (installmentRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal applyAmount = remaining.min(installmentRemaining);
            BigDecimal interestPortion = applyAmount.min(interestRemaining);
            BigDecimal principalPortion = applyAmount.subtract(interestPortion);

            LoanPaymentDetail savedDetail = loanPaymentDetailRepository.save(LoanPaymentDetail.builder()
                    .payment(payment)
                    .scheduleInstallment(installment)
                    .principalAmount(principalPortion)
                    .interestAmount(interestPortion)
                    .penaltyAmount(BigDecimal.ZERO)
                    .build());
            createdDetails.add(savedDetail);

            BigDecimal totalPaidNow = interestPaidSoFar.add(principalPaidSoFar).add(applyAmount);
            installment.setStatus(totalPaidNow.compareTo(installment.getTotalAmount()) >= 0
                    ? ScheduleInstallmentStatus.PAID
                    : ScheduleInstallmentStatus.PARTIALLY_PAID);
            loanScheduleInstallmentRepository.save(installment);

            remaining = remaining.subtract(applyAmount);
        }
        return createdDetails;
    }

    private LoanPaymentResponse toPaymentResponse(LoanPayment payment) {
        List<LoanPaymentDetailResponse> allocations = loanPaymentDetailRepository
                .findByPaymentIdOrderByIdAsc(payment.getId()).stream()
                .map(this::toPaymentDetailResponse)
                .toList();
        return LoanPaymentResponse.builder()
                .id(payment.getId())
                .paymentNo(payment.getPaymentNo())
                .loanId(payment.getLoan().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .method(payment.getMethod())
                .reference(payment.getReference())
                .allocations(allocations)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private LoanPaymentDetailResponse toPaymentDetailResponse(LoanPaymentDetail detail) {
        return LoanPaymentDetailResponse.builder()
                .id(detail.getId())
                .paymentId(detail.getPayment().getId())
                .scheduleDetailId(detail.getScheduleInstallment().getId())
                .installmentNumber(detail.getScheduleInstallment().getInstallmentNumber())
                .principalAllocated(detail.getPrincipalAmount())
                .interestAllocated(detail.getInterestAmount())
                .penaltyAllocated(detail.getPenaltyAmount())
                .createdAt(detail.getCreatedAt())
                .build();
    }

    private Loan findOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", id));
    }

    private String generateLoanNo(Loan loan) {
        String datePart = loan.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "LN-" + datePart + "-" + String.format("%06d", loan.getId());
    }

    private String generatePaymentNo(LoanPayment payment) {
        String datePart = payment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PMT-" + datePart + "-" + String.format("%06d", payment.getId());
    }

    private String generateDisbursementNo(LoanDisbursement disbursement) {
        String datePart = disbursement.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "DSB-" + datePart + "-" + String.format("%06d", disbursement.getId());
    }

    private LoanResponse toResponse(Loan loan, CustomerResponse customer) {
        return LoanResponse.builder()
                .id(loan.getId())
                .loanNo(loan.getLoanNo())
                .customerId(loan.getCustomerId())
                .branchId(loan.getBranchId())
                .customerName(customer != null
                        ? customer.getFirstName() + " " + customer.getLastName()
                        : null)
                .principal(loan.getPrincipal())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .status(loan.getStatus())
                .purpose(loan.getPurpose())
                .approvedAt(loan.getApprovedAt())
                .rejectedAt(loan.getRejectedAt())
                .disbursedAt(loan.getDisbursedAt())
                .closedAt(loan.getClosedAt())
                .maturityDate(loan.getMaturityDate())
                .monthlyInstallment(loan.getMonthlyInstallment())
                .outstandingBalance(loan.getOutstandingBalance())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    private LoanGuarantor findGuarantorOrThrow(Long loanId, Long guarantorId) {
        LoanGuarantor guarantor = loanGuarantorRepository.findById(guarantorId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan guarantor", guarantorId));
        if (!guarantor.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan guarantor", guarantorId);
        }
        return guarantor;
    }

    private LoanCollateral findCollateralOrThrow(Long loanId, Long collateralId) {
        LoanCollateral collateral = loanCollateralRepository.findById(collateralId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan collateral", collateralId));
        if (!collateral.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan collateral", collateralId);
        }
        return collateral;
    }

    private LoanDocument findDocumentOrThrow(Long loanId, Long documentId) {
        LoanDocument document = loanDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan document", documentId));
        if (!document.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan document", documentId);
        }
        return document;
    }

    private LoanPenalty findPenaltyOrThrow(Long loanId, Long penaltyId) {
        LoanPenalty penalty = loanPenaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan penalty", penaltyId));
        if (!penalty.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan penalty", penaltyId);
        }
        return penalty;
    }

    private LoanFee findFeeOrThrow(Long loanId, Long feeId) {
        LoanFee fee = loanFeeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan fee", feeId));
        if (!fee.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan fee", feeId);
        }
        return fee;
    }

    private LoanInterestAccrual findInterestAccrualOrThrow(Long loanId, Long accrualId) {
        LoanInterestAccrual accrual = loanInterestAccrualRepository.findById(accrualId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan interest accrual", accrualId));
        if (!accrual.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan interest accrual", accrualId);
        }
        return accrual;
    }

    private LoanInterestResponse toInterestResponse(LoanInterestAccrual accrual) {
        return LoanInterestResponse.builder()
                .id(accrual.getId())
                .loanId(accrual.getLoan().getId())
                .periodStart(accrual.getPeriodStart())
                .periodEnd(accrual.getPeriodEnd())
                .rate(accrual.getRate())
                .amount(accrual.getAmount())
                .accruedAt(accrual.getAccruedAt())
                .createdAt(accrual.getCreatedAt())
                .updatedAt(accrual.getUpdatedAt())
                .build();
    }

    private LoanPenaltyResponse toPenaltyResponse(LoanPenalty penalty) {
        return LoanPenaltyResponse.builder()
                .id(penalty.getId())
                .loanId(penalty.getLoan().getId())
                .amount(penalty.getAmount())
                .reason(penalty.getReason())
                .appliedDate(penalty.getAppliedDate())
                .status(penalty.getStatus())
                .waivedAt(penalty.getWaivedAt())
                .paidAt(penalty.getPaidAt())
                .createdAt(penalty.getCreatedAt())
                .updatedAt(penalty.getUpdatedAt())
                .build();
    }

    private LoanFeeResponse toFeeResponse(LoanFee fee) {
        return LoanFeeResponse.builder()
                .id(fee.getId())
                .loanId(fee.getLoan().getId())
                .type(fee.getType())
                .amount(fee.getAmount())
                .chargedDate(fee.getChargedDate())
                .description(fee.getDescription())
                .status(fee.getStatus())
                .waivedAt(fee.getWaivedAt())
                .paidAt(fee.getPaidAt())
                .createdAt(fee.getCreatedAt())
                .updatedAt(fee.getUpdatedAt())
                .build();
    }

    private LoanRestructureResponse toRestructureResponse(LoanRestructure restructure) {
        return LoanRestructureResponse.builder()
                .id(restructure.getId())
                .loanId(restructure.getLoan().getId())
                .newTermMonths(restructure.getNewTermMonths())
                .newInterestRate(restructure.getNewInterestRate())
                .reason(restructure.getReason())
                .effectiveDate(restructure.getEffectiveDate())
                .createdAt(restructure.getCreatedAt())
                .updatedAt(restructure.getUpdatedAt())
                .build();
    }

    private LoanRefinanceResponse toRefinanceResponse(LoanRefinance refinance) {
        return LoanRefinanceResponse.builder()
                .id(refinance.getId())
                .loanId(refinance.getLoan().getId())
                .newLoanId(refinance.getNewLoanId())
                .reason(refinance.getReason())
                .effectiveDate(refinance.getEffectiveDate())
                .createdAt(refinance.getCreatedAt())
                .updatedAt(refinance.getUpdatedAt())
                .build();
    }

    private LoanSettlementResponse toSettlementResponse(LoanSettlement settlement) {
        return LoanSettlementResponse.builder()
                .id(settlement.getId())
                .loanId(settlement.getLoan().getId())
                .settlementAmount(settlement.getSettlementAmount())
                .settlementDate(settlement.getSettlementDate())
                .status(settlement.getStatus())
                .note(settlement.getNote())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }

    private LoanWriteoffResponse toWriteoffResponse(LoanWriteoff writeoff) {
        return LoanWriteoffResponse.builder()
                .id(writeoff.getId())
                .loanId(writeoff.getLoan().getId())
                .amount(writeoff.getAmount())
                .reason(writeoff.getReason())
                .writeoffDate(writeoff.getWriteoffDate())
                .status(writeoff.getStatus())
                .createdAt(writeoff.getCreatedAt())
                .updatedAt(writeoff.getUpdatedAt())
                .build();
    }

    private void recordStatusHistory(Loan loan, LoanStatus fromStatus, LoanStatus toStatus, String note) {
        LoanStatusHistory history = LoanStatusHistory.builder()
                .loan(loan)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(note)
                .changedBy(currentUsername())
                .changedAt(LocalDateTime.now())
                .build();
        loanStatusHistoryRepository.save(history);
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private LoanStatusHistoryResponse toStatusHistoryResponse(LoanStatusHistory history) {
        return LoanStatusHistoryResponse.builder()
                .id(history.getId())
                .loanId(history.getLoan().getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .note(history.getNote())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build();
    }

    private LoanDisbursementResponse toDisbursementResponse(LoanDisbursement disbursement) {
        return LoanDisbursementResponse.builder()
                .id(disbursement.getId())
                .disbursementNo(disbursement.getDisbursementNo())
                .loanId(disbursement.getLoan().getId())
                .amount(disbursement.getAmount())
                .disbursedDate(disbursement.getDisbursedDate())
                .method(disbursement.getMethod())
                .reference(disbursement.getReference())
                .status(disbursement.getStatus())
                .createdBy(disbursement.getCreatedBy())
                .reviewedBy(disbursement.getReviewedBy())
                .reviewedAt(disbursement.getReviewedAt())
                .rejectionReason(disbursement.getRejectionReason())
                .voidedBy(disbursement.getVoidedBy())
                .voidedAt(disbursement.getVoidedAt())
                .voidReason(disbursement.getVoidReason())
                .createdAt(disbursement.getCreatedAt())
                .updatedAt(disbursement.getUpdatedAt())
                .build();
    }

    private LoanGuarantorResponse toGuarantorResponse(LoanGuarantor guarantor) {
        return LoanGuarantorResponse.builder()
                .id(guarantor.getId())
                .loanId(guarantor.getLoan().getId())
                .name(guarantor.getName())
                .phone(guarantor.getPhone())
                .relationship(guarantor.getRelationship())
                .guaranteedAmount(guarantor.getGuaranteedAmount())
                .status(guarantor.getStatus())
                .releasedAt(guarantor.getReleasedAt())
                .createdAt(guarantor.getCreatedAt())
                .updatedAt(guarantor.getUpdatedAt())
                .build();
    }

    private LoanCollateralResponse toCollateralResponse(LoanCollateral collateral) {
        return LoanCollateralResponse.builder()
                .id(collateral.getId())
                .loanId(collateral.getLoan().getId())
                .type(collateral.getType())
                .description(collateral.getDescription())
                .estimatedValue(collateral.getEstimatedValue())
                .reference(collateral.getReference())
                .status(collateral.getStatus())
                .releasedAt(collateral.getReleasedAt())
                .createdAt(collateral.getCreatedAt())
                .updatedAt(collateral.getUpdatedAt())
                .build();
    }

    private LoanDocumentResponse toDocumentResponse(LoanDocument document) {
        return LoanDocumentResponse.builder()
                .id(document.getId())
                .loanId(document.getLoan().getId())
                .name(document.getName())
                .status(document.getStatus())
                .notes(document.getNotes())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private LoanNoteResponse toNoteResponse(LoanNote note) {
        return LoanNoteResponse.builder()
                .id(note.getId())
                .loanId(note.getLoan().getId())
                .authorName(note.getAuthorName())
                .note(note.getNote())
                .createdAt(note.getCreatedAt())
                .build();
    }

    // Persists one amortization run: supersedes whatever schedule was
    // previously ACTIVE for this loan (on disbursement there's never a prior
    // one; on restructure there is), then saves the new header + its
    // installment lines with a running balance. startingBalance is the
    // principal the installments were generated against — the loan's
    // original principal on disbursement, or its outstanding balance at the
    // point of restructure (a restructure re-amortizes what's left, not the
    // original principal).
    private void generateAndPersistSchedule(Loan loan, List<AmortizationCalculator.Installment> installments, BigDecimal startingBalance) {
        List<LoanSchedule> activeSchedules = loanScheduleRepository.findByLoanIdAndStatus(loan.getId(), ScheduleStatus.ACTIVE);
        activeSchedules.forEach(s -> s.setStatus(ScheduleStatus.SUPERSEDED));
        loanScheduleRepository.saveAll(activeSchedules);

        LoanSchedule schedule = LoanSchedule.builder()
                .loan(loan)
                .generatedAt(LocalDateTime.now())
                .totalInstallments(installments.size())
                .status(ScheduleStatus.ACTIVE)
                .build();
        LoanSchedule savedSchedule = loanScheduleRepository.save(schedule);

        BigDecimal runningBalance = startingBalance;
        List<LoanScheduleInstallment> lines = new ArrayList<>();
        for (AmortizationCalculator.Installment installment : installments) {
            runningBalance = runningBalance.subtract(installment.principalComponent());
            lines.add(LoanScheduleInstallment.builder()
                    .schedule(savedSchedule)
                    .loan(loan)
                    .installmentNumber(installment.installmentNumber())
                    .dueDate(installment.dueDate())
                    .principalAmount(installment.principalComponent())
                    .interestAmount(installment.interestComponent())
                    .totalAmount(installment.amount())
                    .outstandingBalance(runningBalance.max(BigDecimal.ZERO))
                    .status(ScheduleInstallmentStatus.PENDING)
                    .build());
        }
        loanScheduleInstallmentRepository.saveAll(lines);
    }

    private LoanSchedule findScheduleOrThrow(Long loanId, Long scheduleId) {
        LoanSchedule schedule = loanScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan schedule", scheduleId));
        if (!schedule.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("Loan schedule", scheduleId);
        }
        return schedule;
    }

    private LoanScheduleResponse toScheduleResponse(LoanSchedule schedule) {
        return LoanScheduleResponse.builder()
                .id(schedule.getId())
                .loanId(schedule.getLoan().getId())
                .generatedAt(schedule.getGeneratedAt())
                .totalInstallments(schedule.getTotalInstallments())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .build();
    }

    private LoanScheduleInstallmentResponse toScheduleInstallmentResponse(LoanScheduleInstallment installment) {
        return LoanScheduleInstallmentResponse.builder()
                .id(installment.getId())
                .scheduleId(installment.getSchedule().getId())
                .loanId(installment.getLoan().getId())
                .installmentNumber(installment.getInstallmentNumber())
                .dueDate(installment.getDueDate())
                .principalAmount(installment.getPrincipalAmount())
                .interestAmount(installment.getInterestAmount())
                .totalAmount(installment.getTotalAmount())
                .outstandingBalance(installment.getOutstandingBalance())
                .status(installment.getStatus())
                .createdAt(installment.getCreatedAt())
                .build();
    }
}
