package com.example.loan.service.impl;

import com.example.loan.client.AccountingClient;
import com.example.loan.client.CustomerClient;
import com.example.loan.client.PaymentClient;
import com.example.loan.common.ApiResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.JournalEntryGenerateRequest;
import com.example.loan.dto.JournalEntryResponse;
import com.example.loan.dto.LoanCollateralResponse;
import com.example.loan.dto.LoanRefinanceRequest;
import com.example.loan.dto.LoanRestructureResponse;
import com.example.loan.dto.LoanResponse;
import com.example.loan.dto.LoanPayoffQuoteResponse;
import com.example.loan.dto.LoanPayoffRequest;
import com.example.loan.dto.LoanPaymentResponse;
import com.example.loan.dto.LoanPaymentReverseRequest;
import com.example.loan.dto.LoanWriteoffRecoveryRequest;
import com.example.loan.entity.LoanWriteoff;
import com.example.loan.entity.WriteoffStatus;
import com.example.loan.entity.DisbursementMethod;
import com.example.loan.entity.FeeStatus;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanFee;
import com.example.loan.entity.LoanPayment;
import com.example.loan.entity.LoanPenalty;
import com.example.loan.entity.LoanSchedule;
import com.example.loan.entity.LoanScheduleInstallment;
import com.example.loan.entity.LoanStatus;
import com.example.loan.entity.LoanTransaction;
import com.example.loan.entity.PenaltyStatus;
import com.example.loan.entity.ScheduleInstallmentStatus;
import com.example.loan.entity.ScheduleStatus;
import com.example.loan.exception.AppException;
import com.example.loan.repository.LoanFeeRepository;
import com.example.loan.repository.LoanPaymentDetailRepository;
import com.example.loan.repository.LoanPaymentRepository;
import com.example.loan.repository.LoanPenaltyRepository;
import com.example.loan.repository.LoanRefinanceRepository;
import com.example.loan.repository.LoanRepository;
import com.example.loan.repository.LoanScheduleInstallmentRepository;
import com.example.loan.repository.LoanScheduleRepository;
import com.example.loan.repository.LoanStatusHistoryRepository;
import com.example.loan.repository.LoanTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// LoanServiceImpl is a 20+-dependency god-class (see its @RequiredArgsConstructor field
// list) — @InjectMocks + one @Mock per field lets Mockito wire the constructor without
// hand-ordering 20 positional args. These tests cover the three things fixed this session
// that actually needed regression protection: applyPayment's schedule allocation (used to
// dump the whole amount as PRINCIPAL_PAYMENT, understating interest income), addRefinance
// actually closing the old loan (used to leave both loans ACTIVE forever), and
// recordTransaction's TransactionType mapping to accounting-service (silently wrong here
// means silently wrong journal entries).
@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock private LoanRepository loanRepository;
    @Mock private CustomerClient customerClient;
    @Mock private PaymentClient paymentClient;
    @Mock private AccountingClient accountingClient;
    @Mock private LoanStatusHistoryRepository loanStatusHistoryRepository;
    @Mock private com.example.loan.repository.LoanDisbursementRepository loanDisbursementRepository;
    @Mock private com.example.loan.repository.LoanGuarantorRepository loanGuarantorRepository;
    @Mock private com.example.loan.repository.LoanCollateralRepository loanCollateralRepository;
    @Mock private com.example.loan.repository.LoanDocumentRepository loanDocumentRepository;
    @Mock private com.example.loan.repository.LoanNoteRepository loanNoteRepository;
    @Mock private LoanScheduleRepository loanScheduleRepository;
    @Mock private LoanScheduleInstallmentRepository loanScheduleInstallmentRepository;
    @Mock private LoanPaymentRepository loanPaymentRepository;
    @Mock private LoanPaymentDetailRepository loanPaymentDetailRepository;
    @Mock private com.example.loan.repository.LoanInterestAccrualRepository loanInterestAccrualRepository;
    @Mock private LoanPenaltyRepository loanPenaltyRepository;
    @Mock private LoanFeeRepository loanFeeRepository;
    @Mock private com.example.loan.repository.LoanRestructureRepository loanRestructureRepository;
    @Mock private LoanRefinanceRepository loanRefinanceRepository;
    @Mock private com.example.loan.repository.LoanSettlementRepository loanSettlementRepository;
    @Mock private com.example.loan.repository.LoanWriteoffRepository loanWriteoffRepository;
    @Mock private com.example.loan.repository.LoanWriteoffRecoveryRepository loanWriteoffRecoveryRepository;
    @Mock private com.example.loan.repository.LoanAdjustmentRepository loanAdjustmentRepository;
    @Mock private LoanTransactionRepository loanTransactionRepository;
    @Mock private com.example.loan.notification.LoanNotifier loanNotifier;

    private LoanServiceImpl service;

    private Loan activeLoan;

    @BeforeEach
    void setUp() {
        service = new LoanServiceImpl(loanRepository, customerClient, paymentClient, accountingClient,
                loanStatusHistoryRepository, loanDisbursementRepository, loanGuarantorRepository,
                loanCollateralRepository, loanDocumentRepository, loanNoteRepository, loanScheduleRepository,
                loanScheduleInstallmentRepository, loanPaymentRepository, loanPaymentDetailRepository,
                loanInterestAccrualRepository, loanPenaltyRepository, loanFeeRepository, loanRestructureRepository,
                loanRefinanceRepository, loanSettlementRepository, loanWriteoffRepository,
                loanWriteoffRecoveryRepository, loanAdjustmentRepository, loanTransactionRepository, loanNotifier);

        activeLoan = Loan.builder()
                .customerId(4L)
                .branchId(1L)
                .principal(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("5.00"))
                .termMonths(12)
                .status(LoanStatus.ACTIVE)
                .outstandingBalance(new BigDecimal("977.30"))
                .build();
        activeLoan.setId(7L);

        // Every LoanTransaction save assigns an id, matching recordTransaction's
        // transaction.getId().toString() use as accounting-service's referenceId.
        AtomicLong txnId = new AtomicLong(1);
        lenient().when(loanTransactionRepository.save(any(LoanTransaction.class))).thenAnswer(inv -> {
            LoanTransaction t = inv.getArgument(0);
            if (t.getId() == null) {
                t.setId(txnId.getAndIncrement());
            }
            return t;
        });
        JournalEntryResponse generated = new JournalEntryResponse();
        generated.setId(500L);
        generated.setStatus("POSTED");
        lenient().when(accountingClient.generate(any(JournalEntryGenerateRequest.class)))
                .thenReturn(ApiResponse.success(generated));
        lenient().when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerResponse customer = new CustomerResponse();
        customer.setFirstName("Kim");
        customer.setLastName("Dara");
        lenient().when(customerClient.getById(anyLong())).thenReturn(ApiResponse.success(customer));
    }

    // ── applyPayment: schedule allocation + split accounting postings ──────────────────

    @Test
    void applyPayment_allocatesAgainstScheduleInsteadOfDumpingWholeAmountToPrincipal() {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));

        AtomicLong paymentId = new AtomicLong(1);
        when(loanPaymentRepository.save(any(LoanPayment.class))).thenAnswer(inv -> {
            LoanPayment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(paymentId.getAndIncrement());
                p.setCreatedAt(LocalDateTime.now());
            }
            return p;
        });

        LoanSchedule schedule = LoanSchedule.builder().loan(activeLoan).status(ScheduleStatus.ACTIVE).build();
        schedule.setId(1L);
        when(loanScheduleRepository.findByLoanIdAndStatus(7L, ScheduleStatus.ACTIVE)).thenReturn(List.of(schedule));

        LoanScheduleInstallment installment = LoanScheduleInstallment.builder()
                .schedule(schedule).loan(activeLoan).installmentNumber(1)
                .principalAmount(new BigDecimal("45.83")).interestAmount(new BigDecimal("4.17"))
                .totalAmount(new BigDecimal("50.00")).status(ScheduleInstallmentStatus.PENDING).build();
        installment.setId(115L);
        when(loanScheduleInstallmentRepository.findByScheduleIdAndStatusNotOrderByInstallmentNumberAsc(
                1L, ScheduleInstallmentStatus.PAID)).thenReturn(List.of(installment));
        when(loanPaymentDetailRepository.findByScheduleInstallmentId(115L)).thenReturn(List.of());
        when(loanPaymentDetailRepository.save(any())).thenAnswer(inv -> {
            var detail = (com.example.loan.entity.LoanPaymentDetail) inv.getArgument(0);
            detail.setId(1L);
            return detail;
        });

        LoanResponse response = service.applyPayment(7L, new ApplyPaymentRequest(new BigDecimal("50.00")));

        assertThat(response.getOutstandingBalance()).isEqualByComparingTo("927.30");

        // The old behavior posted the whole $50 as PRINCIPAL_PAYMENT. The fix must post the
        // schedule-derived split instead: $45.83 principal, $4.17 interest, as two entries.
        ArgumentCaptor<JournalEntryGenerateRequest> captor = ArgumentCaptor.forClass(JournalEntryGenerateRequest.class);
        verify(accountingClient, times(2)).generate(captor.capture());
        List<JournalEntryGenerateRequest> requests = captor.getAllValues();
        assertThat(requests).anySatisfy(r -> {
            assertThat(r.getTransactionType()).isEqualTo("PRINCIPAL_PAYMENT");
            assertThat(r.getAmount()).isEqualByComparingTo("45.83");
        });
        assertThat(requests).anySatisfy(r -> {
            assertThat(r.getTransactionType()).isEqualTo("INTEREST_PAYMENT");
            assertThat(r.getAmount()).isEqualByComparingTo("4.17");
        });
        assertThat(requests).noneMatch(r -> r.getAmount().compareTo(new BigDecimal("50.00")) == 0);
    }

    @Test
    void applyPayment_rejectsNonActiveLoan() {
        Loan pending = Loan.builder().status(LoanStatus.PENDING).build();
        pending.setId(9L);
        when(loanRepository.findById(9L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.applyPayment(9L, new ApplyPaymentRequest(new BigDecimal("10.00"))))
                .isInstanceOf(AppException.class);
    }

    // ── payment waterfall: fees, then penalties, then the schedule ─────────────────────

    private void stubNoActiveScheduleAndPaymentPersistence() {
        AtomicLong paymentId = new AtomicLong(1);
        when(loanPaymentRepository.save(any(LoanPayment.class))).thenAnswer(inv -> {
            LoanPayment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(paymentId.getAndIncrement());
                p.setCreatedAt(LocalDateTime.now());
            }
            return p;
        });
        lenient().when(loanScheduleRepository.findByLoanIdAndStatus(7L, ScheduleStatus.ACTIVE)).thenReturn(List.of());
    }

    @Test
    void applyPayment_paysOutstandingFeeBeforeReducingOutstandingBalance() {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        stubNoActiveScheduleAndPaymentPersistence();

        LoanFee pendingFee = LoanFee.builder().loan(activeLoan).amount(new BigDecimal("12.00"))
                .chargedDate(LocalDate.of(2026, 8, 1)).status(FeeStatus.PENDING).build();
        pendingFee.setId(3L);
        when(loanFeeRepository.findByLoanIdOrderByChargedDateAsc(7L)).thenReturn(List.of(pendingFee));
        when(loanFeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(7L)).thenReturn(List.of());

        // $12 covers the fee, the remaining $50 has nowhere to allocate (no active schedule)
        // so it's booked as an unallocated principal reduction — outstandingBalance must drop
        // by only $50, not the full $62, since the fee isn't part of outstandingBalance.
        LoanResponse response = service.applyPayment(7L, new ApplyPaymentRequest(new BigDecimal("62.00")));

        assertThat(pendingFee.getStatus()).isEqualTo(FeeStatus.PAID);
        assertThat(response.getOutstandingBalance()).isEqualByComparingTo("927.30");

        ArgumentCaptor<JournalEntryGenerateRequest> captor = ArgumentCaptor.forClass(JournalEntryGenerateRequest.class);
        verify(accountingClient, times(2)).generate(captor.capture());
        assertThat(captor.getAllValues()).anySatisfy(r -> {
            assertThat(r.getTransactionType()).isEqualTo("FEE_CHARGE");
            assertThat(r.getAmount()).isEqualByComparingTo("12.00");
        });
    }

    @Test
    void applyPayment_leavesFeeUnpaidWhenPaymentIsSmallerThanIt() {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        stubNoActiveScheduleAndPaymentPersistence();

        LoanFee pendingFee = LoanFee.builder().loan(activeLoan).amount(new BigDecimal("12.00"))
                .chargedDate(LocalDate.of(2026, 8, 1)).status(FeeStatus.PENDING).build();
        pendingFee.setId(3L);
        when(loanFeeRepository.findByLoanIdOrderByChargedDateAsc(7L)).thenReturn(List.of(pendingFee));
        when(loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(7L)).thenReturn(List.of());

        // Fees are paid in full or not at all (no partial-paid status) — a $5 payment against
        // a $12 fee must leave the fee PENDING and go entirely toward the schedule instead.
        LoanResponse response = service.applyPayment(7L, new ApplyPaymentRequest(new BigDecimal("5.00")));

        assertThat(pendingFee.getStatus()).isEqualTo(FeeStatus.PENDING);
        verify(loanFeeRepository, never()).save(any());
        assertThat(response.getOutstandingBalance()).isEqualByComparingTo("972.30");
    }

    // ── reversePayment: undoes the schedule allocation and outstandingBalance effect ───

    @Test
    void reversePayment_restoresInstallmentAndOutstandingBalanceAndFlagsReversed() {
        LoanSchedule schedule = LoanSchedule.builder().loan(activeLoan).status(ScheduleStatus.ACTIVE).build();
        schedule.setId(1L);

        LoanScheduleInstallment installment = LoanScheduleInstallment.builder()
                .schedule(schedule).loan(activeLoan).installmentNumber(1)
                .principalAmount(new BigDecimal("45.83")).interestAmount(new BigDecimal("4.17"))
                .totalAmount(new BigDecimal("50.00")).status(ScheduleInstallmentStatus.PAID).build();
        installment.setId(115L);

        LoanPayment payment = LoanPayment.builder()
                .loan(activeLoan).amount(new BigDecimal("50.00")).paymentDate(LocalDate.now())
                .method(DisbursementMethod.CASH).paymentNo("PMT-1").build();
        payment.setId(42L);

        var detail = com.example.loan.entity.LoanPaymentDetail.builder()
                .payment(payment).scheduleInstallment(installment)
                .principalAmount(new BigDecimal("45.83")).interestAmount(new BigDecimal("4.17"))
                .penaltyAmount(BigDecimal.ZERO).build();
        detail.setId(1L);

        when(loanPaymentRepository.findById(42L)).thenReturn(Optional.of(payment));
        when(loanPaymentRepository.save(any(LoanPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanPaymentDetailRepository.findByPaymentIdOrderByIdAsc(42L)).thenReturn(List.of(detail));
        // Still "in the DB" from the repository's point of view — the service itself must
        // exclude it via detail.getPayment().isReversed(), which is true by the time this
        // is queried (reversePayment flips the flag before recomputing installment status).
        when(loanPaymentDetailRepository.findByScheduleInstallmentId(115L)).thenReturn(List.of(detail));
        when(loanScheduleInstallmentRepository.save(any(LoanScheduleInstallment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(loanTransactionRepository.findByReferenceTypeAndReferenceId("LoanPayment", 42L))
                .thenReturn(List.of());

        LoanPaymentReverseRequest request = new LoanPaymentReverseRequest();
        request.setReason("Bounced cheque");

        LoanPaymentResponse response = service.reversePayment(7L, 42L, request);

        assertThat(response.isReversed()).isTrue();
        assertThat(response.getReversalReason()).isEqualTo("Bounced cheque");
        assertThat(response.getReversedAt()).isNotNull();
        assertThat(installment.getStatus()).isEqualTo(ScheduleInstallmentStatus.PENDING);
        // 977.30 (original) + 45.83 + 4.17 restored
        assertThat(activeLoan.getOutstandingBalance()).isEqualByComparingTo("1027.30");

        ArgumentCaptor<LoanTransaction> txnCaptor = ArgumentCaptor.forClass(LoanTransaction.class);
        verify(loanTransactionRepository).save(txnCaptor.capture());
        assertThat(txnCaptor.getValue().getType()).isEqualTo(com.example.loan.entity.TransactionType.ADJUSTMENT);
        assertThat(txnCaptor.getValue().getAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void reversePayment_rejectsAlreadyReversedPayment() {
        LoanPayment payment = LoanPayment.builder().loan(activeLoan).reversed(true).build();
        payment.setId(42L);
        when(loanPaymentRepository.findById(42L)).thenReturn(Optional.of(payment));

        LoanPaymentReverseRequest request = new LoanPaymentReverseRequest();
        request.setReason("dup");

        assertThatThrownBy(() -> service.reversePayment(7L, 42L, request)).isInstanceOf(AppException.class);
    }

    @Test
    void reversePayment_rejectsPaymentBelongingToAnotherLoan() {
        Loan otherLoan = Loan.builder().status(LoanStatus.ACTIVE).build();
        otherLoan.setId(99L);
        LoanPayment payment = LoanPayment.builder().loan(otherLoan).build();
        payment.setId(42L);
        when(loanPaymentRepository.findById(42L)).thenReturn(Optional.of(payment));

        LoanPaymentReverseRequest request = new LoanPaymentReverseRequest();
        request.setReason("wrong loan");

        assertThatThrownBy(() -> service.reversePayment(7L, 42L, request))
                .isInstanceOf(com.example.loan.exception.ResourceNotFoundException.class);
    }

    // ── seizeCollateral: marks SEIZED with a reason, mirrors releaseCollateral ─────────

    @Test
    void seizeCollateral_marksSeizedWithReasonAndTimestamp() {
        var collateral = com.example.loan.entity.LoanCollateral.builder()
                .loan(activeLoan).type(com.example.loan.entity.CollateralType.VEHICLE)
                .description("Toyota Camry 2020").estimatedValue(new BigDecimal("8000.00"))
                .status(com.example.loan.entity.CollateralStatus.PLEDGED).build();
        collateral.setId(3L);
        when(loanCollateralRepository.findById(3L)).thenReturn(Optional.of(collateral));
        when(loanCollateralRepository.save(any(com.example.loan.entity.LoanCollateral.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var request = new com.example.loan.dto.LoanCollateralSeizeRequest();
        request.setReason("90+ days delinquent, borrower unreachable");

        LoanCollateralResponse response = service.seizeCollateral(7L, 3L, request);

        assertThat(response.getStatus()).isEqualTo(com.example.loan.entity.CollateralStatus.SEIZED);
        assertThat(response.getSeizureReason()).isEqualTo("90+ days delinquent, borrower unreachable");
        assertThat(response.getSeizedAt()).isNotNull();
    }

    @Test
    void seizeCollateral_rejectsNonPledgedCollateral() {
        var collateral = com.example.loan.entity.LoanCollateral.builder()
                .loan(activeLoan).type(com.example.loan.entity.CollateralType.VEHICLE)
                .description("Toyota Camry 2020").estimatedValue(new BigDecimal("8000.00"))
                .status(com.example.loan.entity.CollateralStatus.RELEASED).build();
        collateral.setId(3L);
        when(loanCollateralRepository.findById(3L)).thenReturn(Optional.of(collateral));

        var request = new com.example.loan.dto.LoanCollateralSeizeRequest();
        request.setReason("too late");

        assertThatThrownBy(() -> service.seizeCollateral(7L, 3L, request)).isInstanceOf(AppException.class);
    }

    // ── restructure approval: addRestructure only requests, approveRestructure applies ─

    @Test
    void addRestructure_onlyRecordsTheRequest_doesNotTouchTheLoanYet() {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        when(loanRestructureRepository.save(any(com.example.loan.entity.LoanRestructure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var request = new com.example.loan.dto.LoanRestructureRequest();
        request.setNewTermMonths(24);
        request.setNewInterestRate(new BigDecimal("4.50"));
        request.setReason("Borrower requested lower payment");
        request.setEffectiveDate(LocalDate.of(2026, 9, 1));

        LoanRestructureResponse response = service.addRestructure(7L, request);

        assertThat(response.getStatus()).isEqualTo(com.example.loan.entity.RestructureStatus.PENDING_APPROVAL);
        // Original term/rate untouched until approval.
        assertThat(activeLoan.getTermMonths()).isEqualTo(12);
        assertThat(activeLoan.getInterestRate()).isEqualByComparingTo("5.00");
        verify(loanScheduleRepository, never()).save(any());
    }

    @Test
    void approveRestructure_appliesNewTermAndRateAndRegeneratesSchedule() {
        activeLoan.setTermMonths(12);
        activeLoan.setInterestRate(new BigDecimal("5.00"));

        var restructure = com.example.loan.entity.LoanRestructure.builder()
                .loan(activeLoan).newTermMonths(24).newInterestRate(new BigDecimal("4.50"))
                .reason("Lower payment").effectiveDate(LocalDate.of(2026, 9, 1))
                .status(com.example.loan.entity.RestructureStatus.PENDING_APPROVAL)
                .createdBy("maker").build();
        restructure.setId(1L);
        when(loanRestructureRepository.findById(1L)).thenReturn(Optional.of(restructure));
        when(loanRestructureRepository.save(any(com.example.loan.entity.LoanRestructure.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(loanScheduleRepository.findByLoanIdAndStatus(7L, ScheduleStatus.ACTIVE)).thenReturn(List.of());
        when(loanScheduleRepository.save(any(LoanSchedule.class))).thenAnswer(inv -> {
            LoanSchedule s = inv.getArgument(0);
            s.setId(9L);
            return s;
        });

        LoanRestructureResponse response = service.approveRestructure(7L, 1L);

        assertThat(response.getStatus()).isEqualTo(com.example.loan.entity.RestructureStatus.APPROVED);
        assertThat(response.getReviewedAt()).isNotNull();
        assertThat(activeLoan.getTermMonths()).isEqualTo(24);
        assertThat(activeLoan.getInterestRate()).isEqualByComparingTo("4.50");
        assertThat(activeLoan.getMaturityDate()).isEqualTo(LocalDate.of(2028, 9, 1));
        verify(loanScheduleInstallmentRepository).saveAll(any());
    }

    @Test
    void approveRestructure_rejectsSameUserApprovingTheirOwnRequest() {
        var restructure = com.example.loan.entity.LoanRestructure.builder()
                .loan(activeLoan).newTermMonths(24).reason("x").effectiveDate(LocalDate.now())
                .status(com.example.loan.entity.RestructureStatus.PENDING_APPROVAL)
                .createdBy("system").build();
        restructure.setId(1L);
        when(loanRestructureRepository.findById(1L)).thenReturn(Optional.of(restructure));

        // currentUsername() falls back to "system" with no authenticated principal in
        // this test context, same as the requester — exercises the maker-checker guard.
        assertThatThrownBy(() -> service.approveRestructure(7L, 1L)).isInstanceOf(AppException.class);
    }

    @Test
    void rejectRestructure_marksRejectedWithReasonAndDoesNotTouchTheLoan() {
        var restructure = com.example.loan.entity.LoanRestructure.builder()
                .loan(activeLoan).newTermMonths(24).reason("x").effectiveDate(LocalDate.now())
                .status(com.example.loan.entity.RestructureStatus.PENDING_APPROVAL)
                .createdBy("maker").build();
        restructure.setId(1L);
        when(loanRestructureRepository.findById(1L)).thenReturn(Optional.of(restructure));
        when(loanRestructureRepository.save(any(com.example.loan.entity.LoanRestructure.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var request = new com.example.loan.dto.LoanRestructureRejectRequest();
        request.setReason("Term extension too aggressive");

        LoanRestructureResponse response = service.rejectRestructure(7L, 1L, request);

        assertThat(response.getStatus()).isEqualTo(com.example.loan.entity.RestructureStatus.REJECTED);
        assertThat(response.getRejectionReason()).isEqualTo("Term extension too aggressive");
        assertThat(activeLoan.getTermMonths()).isEqualTo(12);
        verify(loanScheduleRepository, never()).save(any());
    }

    // ── disburse: notifies the customer once the loan goes ACTIVE ──────────────────────

    @Test
    void disburse_notifiesCustomerOnceLoanIsActive() {
        Loan approvedLoan = Loan.builder()
                .customerId(4L).branchId(1L).loanNo("LN-1")
                .principal(new BigDecimal("1000.00")).interestRate(new BigDecimal("5.00")).termMonths(12)
                .status(LoanStatus.APPROVED).build();
        approvedLoan.setId(8L);
        when(loanRepository.findById(8L)).thenReturn(Optional.of(approvedLoan));
        when(loanScheduleRepository.findByLoanIdAndStatus(8L, ScheduleStatus.ACTIVE)).thenReturn(List.of());
        when(loanScheduleRepository.save(any(LoanSchedule.class))).thenAnswer(inv -> {
            LoanSchedule s = inv.getArgument(0);
            s.setId(11L);
            return s;
        });

        CustomerResponse customer = new CustomerResponse();
        customer.setEmail("dara@example.com");
        customer.setPhone("+855123456789");
        when(customerClient.getById(4L)).thenReturn(ApiResponse.success(customer));

        service.disburse(8L);

        assertThat(approvedLoan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        ArgumentCaptor<CustomerResponse> customerCaptor = ArgumentCaptor.forClass(CustomerResponse.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(loanNotifier).notify(customerCaptor.capture(), any(), messageCaptor.capture());
        assertThat(customerCaptor.getValue()).isSameAs(customer);
        assertThat(messageCaptor.getValue()).contains("LN-1").contains("1000.00");
    }

    // ── addRefinance: must close the old loan, not just leave a note ───────────────────

    @Test
    void addRefinance_closesOldLoanAndBooksLocalAdjustment_withoutCallingAccounting() {
        Loan newLoan = Loan.builder().customerId(4L).status(LoanStatus.ACTIVE).build();
        newLoan.setId(20L);
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.findById(20L)).thenReturn(Optional.of(newLoan));
        when(loanRefinanceRepository.save(any())).thenAnswer(inv -> {
            var r = (com.example.loan.entity.LoanRefinance) inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        LoanRefinanceRequest request = new LoanRefinanceRequest();
        request.setNewLoanId(20L);
        request.setReason("Consolidating");
        request.setEffectiveDate(LocalDate.of(2026, 8, 8));

        service.addRefinance(7L, request);

        assertThat(activeLoan.getStatus()).isEqualTo(LoanStatus.CLOSED);
        assertThat(activeLoan.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(activeLoan.getClosedAt()).isNotNull();
        // ADJUSTMENT has no accounting-service TransactionType equivalent (see
        // toAccountingTransactionType) — the payoff must stay loan-service-local.
        verify(accountingClient, never()).generate(any());
        verify(loanTransactionRepository).save(any(LoanTransaction.class));
    }

    @Test
    void addRefinance_rejectsRefinancingIntoItself() {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        LoanRefinanceRequest request = new LoanRefinanceRequest();
        request.setNewLoanId(7L);
        request.setReason("bad");
        request.setEffectiveDate(LocalDate.now());

        assertThatThrownBy(() -> service.addRefinance(7L, request)).isInstanceOf(AppException.class);
    }

    @Test
    void addRefinance_rejectsDifferentCustomer() {
        Loan otherCustomersLoan = Loan.builder().customerId(999L).status(LoanStatus.ACTIVE).build();
        otherCustomersLoan.setId(21L);
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.findById(21L)).thenReturn(Optional.of(otherCustomersLoan));

        LoanRefinanceRequest request = new LoanRefinanceRequest();
        request.setNewLoanId(21L);
        request.setReason("wrong customer");
        request.setEffectiveDate(LocalDate.now());

        assertThatThrownBy(() -> service.addRefinance(7L, request)).isInstanceOf(AppException.class);
    }

    // ── recordTransaction: TransactionType mapping to accounting-service ───────────────

    @Test
    void payPenalty_mapsToAccountingPenaltyCharge() {
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        LoanPenalty penalty = LoanPenalty.builder()
                .loan(activeLoan).amount(new BigDecimal("12.00")).reason("Late fee")
                .status(PenaltyStatus.PENDING).build();
        penalty.setId(2L);
        when(loanPenaltyRepository.findById(2L)).thenReturn(Optional.of(penalty));
        when(loanPenaltyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.payPenalty(7L, 2L);

        ArgumentCaptor<JournalEntryGenerateRequest> captor = ArgumentCaptor.forClass(JournalEntryGenerateRequest.class);
        verify(accountingClient).generate(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo("PENALTY_CHARGE");
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("12.00");
    }

    // ── payoff quote: must be less than outstandingBalance for an early payoff ─────────

    @Test
    void getPayoffQuote_accruesSimpleDailyInterestSinceDisbursementWhenNoPaymentsYet() {
        activeLoan.setPrincipal(new BigDecimal("1000.00"));
        activeLoan.setInterestRate(new BigDecimal("12.00"));
        activeLoan.setDisbursedAt(LocalDateTime.now().minusDays(30));
        // Fixture's default outstandingBalance (977.30) is leftover from other tests'
        // scenarios — set it here to what disburse() actually sets it to: principal plus
        // every future installment's interest for the full term (see disburse()), which for
        // a fresh 1000/12%/12mo loan is well above 1000 and is exactly the number a 30-day-old
        // payoff should undercut.
        activeLoan.setOutstandingBalance(new BigDecimal("1065.00"));
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        when(loanPaymentDetailRepository.findByPayment_LoanId(7L)).thenReturn(List.of());
        when(loanPaymentRepository.findByLoanIdOrderByPaymentDateAsc(7L)).thenReturn(List.of());
        when(loanFeeRepository.findByLoanIdOrderByChargedDateAsc(7L)).thenReturn(List.of());
        when(loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(7L)).thenReturn(List.of());

        LoanPayoffQuoteResponse quote = service.getPayoffQuote(7L);

        long daysAccrued = java.time.temporal.ChronoUnit.DAYS.between(
                activeLoan.getDisbursedAt().toLocalDate(), LocalDate.now());
        BigDecimal dailyRate = new BigDecimal("12.00")
                .divide(BigDecimal.valueOf(100), 10, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(365), 10, java.math.RoundingMode.HALF_UP);
        BigDecimal expectedInterest = new BigDecimal("1000.00").multiply(dailyRate)
                .multiply(BigDecimal.valueOf(daysAccrued)).setScale(2, java.math.RoundingMode.HALF_UP);

        assertThat(quote.getRemainingPrincipal()).isEqualByComparingTo("1000.00");
        assertThat(quote.getAccruedInterest()).isEqualByComparingTo(expectedInterest);
        // The whole point: this must be less than what outstandingBalance would charge (which
        // bakes in a full 12 months of interest regardless of a 30-day-old loan).
        assertThat(quote.getTotalPayoffAmount()).isLessThan(activeLoan.getOutstandingBalance());
    }

    @Test
    void getPayoffQuote_subtractsPrincipalAlreadyPaidAndIncludesPendingFeesAndPenalties() {
        activeLoan.setPrincipal(new BigDecimal("1000.00"));
        activeLoan.setDisbursedAt(LocalDateTime.now().minusDays(10));
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));

        var alreadyPaidDetail = com.example.loan.entity.LoanPaymentDetail.builder()
                .principalAmount(new BigDecimal("200.00")).interestAmount(new BigDecimal("5.00")).build();
        when(loanPaymentDetailRepository.findByPayment_LoanId(7L)).thenReturn(List.of(alreadyPaidDetail));
        when(loanPaymentRepository.findByLoanIdOrderByPaymentDateAsc(7L)).thenReturn(List.of());

        LoanFee pendingFee = LoanFee.builder().amount(new BigDecimal("15.00")).status(FeeStatus.PENDING).build();
        LoanFee paidFee = LoanFee.builder().amount(new BigDecimal("999.00")).status(FeeStatus.PAID).build();
        when(loanFeeRepository.findByLoanIdOrderByChargedDateAsc(7L)).thenReturn(List.of(pendingFee, paidFee));

        LoanPenalty pendingPenalty = LoanPenalty.builder().amount(new BigDecimal("8.00")).status(PenaltyStatus.PENDING).build();
        when(loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(7L)).thenReturn(List.of(pendingPenalty));

        LoanPayoffQuoteResponse quote = service.getPayoffQuote(7L);

        assertThat(quote.getRemainingPrincipal()).isEqualByComparingTo("800.00");
        assertThat(quote.getOutstandingFees()).isEqualByComparingTo("15.00");
        assertThat(quote.getOutstandingPenalties()).isEqualByComparingTo("8.00");
    }

    @Test
    void getPayoffQuote_rejectsNonActiveLoan() {
        Loan closed = Loan.builder().status(LoanStatus.CLOSED).build();
        closed.setId(9L);
        when(loanRepository.findById(9L)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> service.getPayoffQuote(9L)).isInstanceOf(AppException.class);
    }

    @Test
    void payoff_closesLoanAndSupersedesScheduleAndPostsPrincipalAndInterest() {
        activeLoan.setPrincipal(new BigDecimal("1000.00"));
        activeLoan.setInterestRate(new BigDecimal("12.00"));
        activeLoan.setDisbursedAt(LocalDateTime.now().minusDays(30));
        when(loanRepository.findById(7L)).thenReturn(Optional.of(activeLoan));
        when(loanPaymentDetailRepository.findByPayment_LoanId(7L)).thenReturn(List.of());
        when(loanPaymentRepository.findByLoanIdOrderByPaymentDateAsc(7L)).thenReturn(List.of());
        when(loanFeeRepository.findByLoanIdOrderByChargedDateAsc(7L)).thenReturn(List.of());
        when(loanPenaltyRepository.findByLoanIdOrderByAppliedDateAsc(7L)).thenReturn(List.of());

        AtomicLong paymentId = new AtomicLong(1);
        when(loanPaymentRepository.save(any(LoanPayment.class))).thenAnswer(inv -> {
            LoanPayment p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(paymentId.getAndIncrement());
                p.setCreatedAt(LocalDateTime.now());
            }
            return p;
        });

        LoanSchedule active = LoanSchedule.builder().loan(activeLoan).status(ScheduleStatus.ACTIVE).build();
        active.setId(1L);
        when(loanScheduleRepository.findByLoanIdAndStatus(7L, ScheduleStatus.ACTIVE)).thenReturn(List.of(active));
        when(loanScheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanPayoffRequest request = new LoanPayoffRequest();
        request.setMethod(DisbursementMethod.BANK_TRANSFER);

        LoanResponse response = service.payoff(7L, request);

        assertThat(response.getStatus()).isEqualTo(LoanStatus.CLOSED);
        assertThat(response.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(active.getStatus()).isEqualTo(ScheduleStatus.SUPERSEDED);

        ArgumentCaptor<JournalEntryGenerateRequest> captor = ArgumentCaptor.forClass(JournalEntryGenerateRequest.class);
        verify(accountingClient, times(2)).generate(captor.capture());
        assertThat(captor.getAllValues()).anySatisfy(r -> {
            assertThat(r.getTransactionType()).isEqualTo("PRINCIPAL_PAYMENT");
            assertThat(r.getAmount()).isEqualByComparingTo("1000.00");
        });
        assertThat(captor.getAllValues()).anySatisfy(r -> assertThat(r.getTransactionType()).isEqualTo("INTEREST_PAYMENT"));
    }

    // ── write-off recovery: doesn't reopen the loan, doesn't exceed the written-off amount ──

    @Test
    void recordWriteoffRecovery_postsRecoveryIncomeWithoutReopeningTheLoan() {
        Loan closedLoan = Loan.builder().status(LoanStatus.CLOSED).build();
        closedLoan.setId(7L);
        when(loanRepository.findById(7L)).thenReturn(Optional.of(closedLoan));

        LoanWriteoff writeoff = LoanWriteoff.builder()
                .loan(closedLoan).amount(new BigDecimal("500.00"))
                .reason("Uncollectable").writeoffDate(LocalDate.of(2026, 1, 1))
                .status(WriteoffStatus.COMPLETED).build();
        writeoff.setId(4L);
        when(loanWriteoffRepository.findByLoanId(7L)).thenReturn(Optional.of(writeoff));
        when(loanWriteoffRecoveryRepository.findByWriteoffIdOrderByRecoveryDateAsc(4L)).thenReturn(List.of());
        when(loanWriteoffRecoveryRepository.save(any())).thenAnswer(inv -> {
            var r = (com.example.loan.entity.LoanWriteoffRecovery) inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        LoanWriteoffRecoveryRequest request = new LoanWriteoffRecoveryRequest();
        request.setAmount(new BigDecimal("200.00"));
        request.setRecoveryDate(LocalDate.of(2026, 8, 8));
        request.setMethod(DisbursementMethod.CASH);

        service.recordWriteoffRecovery(7L, request);

        // Recovering a written-off debt doesn't reactivate the loan.
        assertThat(closedLoan.getStatus()).isEqualTo(LoanStatus.CLOSED);

        ArgumentCaptor<JournalEntryGenerateRequest> captor = ArgumentCaptor.forClass(JournalEntryGenerateRequest.class);
        verify(accountingClient).generate(captor.capture());
        assertThat(captor.getValue().getTransactionType()).isEqualTo("RECOVERY");
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void recordWriteoffRecovery_rejectsAmountExceedingWhatWasWrittenOff() {
        Loan closedLoan = Loan.builder().status(LoanStatus.CLOSED).build();
        closedLoan.setId(7L);
        when(loanRepository.findById(7L)).thenReturn(Optional.of(closedLoan));

        LoanWriteoff writeoff = LoanWriteoff.builder()
                .loan(closedLoan).amount(new BigDecimal("500.00"))
                .reason("Uncollectable").writeoffDate(LocalDate.of(2026, 1, 1))
                .status(WriteoffStatus.COMPLETED).build();
        writeoff.setId(4L);
        when(loanWriteoffRepository.findByLoanId(7L)).thenReturn(Optional.of(writeoff));

        // 300 already recovered + a new 250 request would total 550, exceeding the 500 written off.
        var existingRecovery = com.example.loan.entity.LoanWriteoffRecovery.builder()
                .amount(new BigDecimal("300.00")).build();
        when(loanWriteoffRecoveryRepository.findByWriteoffIdOrderByRecoveryDateAsc(4L))
                .thenReturn(List.of(existingRecovery));

        LoanWriteoffRecoveryRequest request = new LoanWriteoffRecoveryRequest();
        request.setAmount(new BigDecimal("250.00"));
        request.setRecoveryDate(LocalDate.of(2026, 8, 8));
        request.setMethod(DisbursementMethod.CASH);

        assertThatThrownBy(() -> service.recordWriteoffRecovery(7L, request)).isInstanceOf(AppException.class);
        verify(loanWriteoffRecoveryRepository, never()).save(any());
    }

    @Test
    void recordWriteoffRecovery_rejectsWhenWriteoffIsStillPending() {
        Loan closedLoan = Loan.builder().status(LoanStatus.CLOSED).build();
        closedLoan.setId(7L);
        when(loanRepository.findById(7L)).thenReturn(Optional.of(closedLoan));

        LoanWriteoff writeoff = LoanWriteoff.builder()
                .loan(closedLoan).amount(new BigDecimal("500.00"))
                .reason("Uncollectable").writeoffDate(LocalDate.of(2026, 1, 1))
                .status(WriteoffStatus.PENDING).build();
        writeoff.setId(4L);
        when(loanWriteoffRepository.findByLoanId(7L)).thenReturn(Optional.of(writeoff));

        LoanWriteoffRecoveryRequest request = new LoanWriteoffRecoveryRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setRecoveryDate(LocalDate.of(2026, 8, 8));
        request.setMethod(DisbursementMethod.CASH);

        assertThatThrownBy(() -> service.recordWriteoffRecovery(7L, request)).isInstanceOf(AppException.class);
    }
}
