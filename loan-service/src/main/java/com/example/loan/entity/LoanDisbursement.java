package com.example.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_disbursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDisbursement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // System-generated tracking number, distinct from the free-text `reference`
    // below (a bank transfer/cheque number entered by the creator). Assigned
    // after the initial save, once the id is known — same two-phase-save
    // pattern as Loan.loanNo, including the same updatable=false gotcha: a plain
    // entity save() can't write it after the row exists (Hibernate excludes
    // non-updatable columns from generated UPDATEs) — see
    // LoanDisbursementRepository.updateDisbursementNo, a bulk JPQL update, which
    // is the only way around that. Nullable because ddl-auto=update can't add a
    // NOT NULL column to a table that already has rows; LoanDisbursementNoBackfill
    // fills in rows that predate this fix.
    @Column(unique = true, updatable = false)
    private String disbursementNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "disbursed_date", nullable = false)
    private LocalDate disbursedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisbursementMethod method;

    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DisbursementStatus status = DisbursementStatus.PENDING_APPROVAL;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "voided_by")
    private String voidedBy;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    @Column(name = "void_reason")
    private String voidReason;

    // accounting-service JournalEntry id created for this disbursement's approval (see
    // LoanServiceImpl.approveDisbursement/recordTransaction) — voidDisbursement uses this
    // to reverse that specific entry instead of just booking a local ADJUSTMENT. Null for
    // disbursements approved before this existed, or if no accounting scheme was configured
    // at approval time (recordTransaction only records locally in that case).
    @Column(name = "journal_entry_id")
    private Long journalEntryId;
}
