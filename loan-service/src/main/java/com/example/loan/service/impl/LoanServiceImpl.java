package com.example.loan.service.impl;

import com.example.loan.client.CustomerClient;
import com.example.loan.client.PaymentClient;
import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.GenerateScheduleRequest;
import com.example.loan.dto.LoanAdjustmentRequest;
import com.example.loan.dto.LoanAdjustmentResponse;
import com.example.loan.dto.LoanCollateralRequest;
import com.example.loan.dto.LoanCollateralResponse;
import com.example.loan.dto.LoanDisbursementRequest;
import com.example.loan.dto.LoanDisbursementResponse;
import com.example.loan.dto.LoanFeeRequest;
import com.example.loan.dto.LoanFeeResponse;
import com.example.loan.dto.LoanGuarantorRequest;
import com.example.loan.dto.LoanGuarantorResponse;
import com.example.loan.dto.LoanInterestRequest;
import com.example.loan.dto.LoanInterestResponse;
import com.example.loan.dto.LoanPaymentDetailResponse;
import com.example.loan.dto.LoanPaymentRequest;
import com.example.loan.dto.LoanPaymentResponse;
import com.example.loan.dto.LoanPenaltyRequest;
import com.example.loan.dto.LoanPenaltyResponse;
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
import com.example.loan.dto.LoanWriteoffRequest;
import com.example.loan.dto.LoanWriteoffResponse;
import com.example.loan.dto.ScheduleInstallmentRequest;
import com.example.loan.entity.AdjustmentType;
import com.example.loan.entity.CollateralStatus;
import com.example.loan.entity.FeeStatus;
import com.example.loan.entity.GuarantorStatus;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanAdjustment;
import com.example.loan.entity.LoanCollateral;
import com.example.loan.entity.LoanDisbursement;
import com.example.loan.entity.LoanFee;
import com.example.loan.entity.LoanGuarantor;
import com.example.loan.entity.LoanInterestAccrual;
import com.example.loan.entity.LoanPayment;
import com.example.loan.entity.LoanPaymentDetail;
import com.example.loan.entity.LoanPenalty;
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
import com.example.loan.repository.LoanDisbursementRepository;
import com.example.loan.repository.LoanFeeRepository;
import com.example.loan.repository.LoanGuarantorRepository;
import com.example.loan.repository.LoanInterestAccrualRepository;
import com.example.loan.repository.LoanPaymentDetailRepository;
import com.example.loan.repository.LoanPaymentRepository;
import com.example.loan.repository.LoanPenaltyRepository;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.LoanRestructureRepository;
import com.example.loan.repository.LoanScheduleInstallmentRepository;
import com.example.loan.repository.LoanScheduleRepository;
import com.example.loan.repository.LoanSettlementRepository;
import com.example.loan.repository.LoanStatusHistoryRepository;
import com.example.loan.repository.LoanTransactionRepository;
import com.example.loan.repository.LoanWriteoffRepository;
import com.example.loan.service.LoanService;
import com.example.loan.util.AmortizationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;
    private final LoanDisbursementRepository loanDisbursementRepository;
    private final LoanGuarantorRepository loanGuarantorRepository;
    private final LoanCollateralRepository loanCollateralRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanScheduleInstallmentRepository loanScheduleInstallmentRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final LoanPaymentDetailRepository loanPaymentDetailRepository;
    private final LoanInterestAccrualRepository loanInterestAccrualRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final LoanFeeRepository loanFeeRepository;
    private final LoanRestructureRepository loanRestructureRepository;
    private final LoanSettlementRepository loanSettlementRepository;
    private final LoanWriteoffRepository loanWriteoffRepository;
    private final LoanAdjustmentRepository loanAdjustmentRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public LoanResponse create(LoanRequest request) {
        CustomerResponse customer = customerClient.getById(request.getCustomerId());

        Loan loan = Loan.builder()
                .customerId(request.getCustomerId())
                .principal(request.getPrincipal())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .purpose(request.getPurpose())
                .build();

        Loan saved = loanRepository.save(loan);
        recordStatusHistory(saved, null, LoanStatus.PENDING, null);
        return toResponse(saved, customer);
    }

    @Override
    public LoanResponse getById(Long id) {
        Loan loan = findOrThrow(id);
        CustomerResponse customer = customerClient.getById(loan.getCustomerId());
        return toResponse(loan, customer);
    }

    @Override
    public PageResponse<LoanResponse> getAll(int page, int size, String sortBy, String sortOrder) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        return PageResponse.of(loanRepository.findAll(pageable)
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId()))));
    }

    @Override
    public List<LoanResponse> getByCustomer(Long customerId) {
        customerClient.getById(customerId);
        return loanRepository.findByCustomerId(customerId).stream()
                .map(loan -> toResponse(loan, customerClient.getById(loan.getCustomerId())))
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
        CustomerResponse customer = customerClient.getById(saved.getCustomerId());
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
        CustomerResponse customer = customerClient.getById(saved.getCustomerId());
        return toResponse(saved, customer);
    }

    @Override
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

        CustomerResponse customer = customerClient.getById(saved.getCustomerId());
        return toResponse(saved, customer);
    }

    @Override
    public LoanResponse applyPayment(Long id, ApplyPaymentRequest request) {
        Loan loan = findOrThrow(id);
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new AppException(HttpStatus.CONFLICT, "Payments can only be applied to ACTIVE loans");
        }
        BigDecimal newBalance = loan.getOutstandingBalance().subtract(request.getAmount());
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
        recordTransaction(saved, TransactionType.PRINCIPAL_PAYMENT, request.getAmount(), LocalDate.now(),
                "Loan", saved.getId(), "Applied via legacy apply-payment action");
        CustomerResponse customer = customerClient.getById(saved.getCustomerId());
        return toResponse(saved, customer);
    }

    @Override
    public void delete(Long id) {
        loanRepository.delete(findOrThrow(id));
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
        LoanDisbursement disbursement = LoanDisbursement.builder()
                .loan(loan)
                .amount(request.getAmount())
                .disbursedDate(request.getDisbursedDate())
                .method(request.getMethod())
                .reference(request.getReference())
                .build();
        LoanDisbursement saved = loanDisbursementRepository.save(disbursement);
        recordTransaction(loan, TransactionType.DISBURSEMENT, saved.getAmount(), saved.getDisbursedDate(),
                "LoanDisbursement", saved.getId(), saved.getReference());
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

        List<LoanPaymentDetail> details = allocatePayment(loan, savedPayment, request.getAmount());

        BigDecimal newBalance = loan.getOutstandingBalance().subtract(request.getAmount());
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
        BigDecimal unallocated = request.getAmount().subtract(totalAllocated);
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
    private void recordTransaction(Loan loan, TransactionType type, BigDecimal amount, LocalDate transactionDate,
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
        loanTransactionRepository.save(transaction);
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

    // Waterfall allocation against the loan's ACTIVE schedule: oldest unpaid
    // installment first, interest before principal within each installment.
    // No-ops (payment recorded, nothing allocated) if the loan has no ACTIVE
    // schedule yet. Penalties aren't wired into this pass — LoanPenalty has
    // its own pay/waive actions on a separate ledger.
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

    private LoanResponse toResponse(Loan loan, CustomerResponse customer) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomerId())
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
                .loanId(disbursement.getLoan().getId())
                .amount(disbursement.getAmount())
                .disbursedDate(disbursement.getDisbursedDate())
                .method(disbursement.getMethod())
                .reference(disbursement.getReference())
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
