package com.example.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assigned after the initial save, once the id is known — same two-phase-save
    // pattern as Loan.loanNo. updatable=false is deliberate (this is a system-issued
    // reference number, immutable once set) but it also means a plain entity save()
    // can never write it after the row exists, since Hibernate excludes non-updatable
    // columns from generated UPDATE statements — see ApplicationRepository.updateApplicationNo,
    // a bulk JPQL update, which is the only way around that. Nullable because
    // ddl-auto=update can't add a NOT NULL column to a table that already has rows;
    // ApplicationNoBackfill fills in rows that predate this column.
    @Column(unique = true, updatable = false)
    private String applicationNo;

    @Column(nullable = false)
    private Long customerId;

    // Copied from the customer's branchId at creation (see ApplicationServiceImpl.create)
    // — raw cross-service id, no FK, same unvalidated convention as customerId.
    private Long branchId;

    // Cross-service id (loan-product-service) — populated at submission and validated
    // against the product's amount/term range in ApplicationServiceImpl.create()/update().
    // Nullable because ddl-auto=update can't add a NOT NULL column to a table that already
    // has rows (same reasoning as applicationNo above); required-ness is instead enforced
    // by ApplicationRequest.loanProductId's @NotNull.
    @Column(name = "loan_product_id")
    private UUID loanProductId;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    // Interpreted per requestedTermUnit: a literal month count for MONTH, a year
    // count for YEAR (multiplied by 12 at schedule-generation time), or a day
    // count for DAY (a single bullet repayment instead of a monthly schedule).
    @Column(name = "requested_term_months", nullable = false)
    private Integer requestedTermMonths;

    // Default backfills existing rows when Hibernate adds this NOT NULL column
    // to a non-empty table under ddl-auto=update.
    @Enumerated(EnumType.STRING)
    @Column(name = "requested_term_unit", nullable = false, columnDefinition = "varchar(10) default 'MONTH'")
    @Builder.Default
    private TermUnit requestedTermUnit = TermUnit.MONTH;

    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    // Set once an approval decision creates the actual Loan record.
    @Column(name = "loan_id")
    private Long loanId;
}
