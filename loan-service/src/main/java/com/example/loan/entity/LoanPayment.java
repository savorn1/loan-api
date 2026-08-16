package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Money received against a loan, tracked directly in loan-service — distinct
// from payment-service's installment ledger used on the Overview tab. On
// creation the amount is walked across the loan's ACTIVE schedule (oldest
// unpaid installment first, interest before principal) producing the
// LoanPaymentDetail breakdown, and the loan's outstandingBalance is reduced.
@Entity
@Table(name = "loan_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // System-generated receipt number, distinct from the free-text `reference`
    // below (a bank slip/cheque number the teller types in). Assigned after the
    // initial save, once the id is known — same two-phase-save pattern as
    // Loan.loanNo, including the same updatable=false gotcha: a plain entity
    // save() can't write it after the row exists (Hibernate excludes non-updatable
    // columns from generated UPDATEs) — see LoanPaymentRepository.updatePaymentNo,
    // a bulk JPQL update, which is the only way around that. Nullable because
    // ddl-auto=update can't add a NOT NULL column to a table that already has rows;
    // LoanPaymentNoBackfill fills in rows that predate this fix.
    @Column(unique = true, updatable = false)
    private String paymentNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisbursementMethod method;

    private String reference;

    // Maker-checker, same shape as LoanRestructure: reversePayment (maker) only sets
    // status/reason/requestedBy/requestedAt; approvePaymentReversal (checker, a
    // different admin — see LoanServiceImpl.assertDifferentFromCreator) is what
    // actually restores the schedule/balance. Null means no reversal was ever requested.
    @Enumerated(EnumType.STRING)
    @Column(name = "reversal_status")
    private PaymentReversalStatus reversalStatus;

    @Column(name = "reversal_reason")
    private String reversalReason;

    @Column(name = "reversal_requested_by")
    private String reversalRequestedBy;

    @Column(name = "reversal_requested_at")
    private LocalDateTime reversalRequestedAt;

    @Column(name = "reversal_reviewed_by")
    private String reversalReviewedBy;

    @Column(name = "reversal_reviewed_at")
    private LocalDateTime reversalReviewedAt;

    @Column(name = "reversal_rejection_reason")
    private String reversalRejectionReason;

    // Derived rather than its own column — APPROVED is the only status that actually
    // reversed anything, so this is the single check every other computation
    // (allocatePayment's paid-so-far filter, outstandingBalance restoration) relies on.
    public boolean isReversed() {
        return reversalStatus == PaymentReversalStatus.APPROVED;
    }
}
