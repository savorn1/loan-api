package com.example.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assigned after the initial save, once the id is known — same two-phase-save
    // pattern as customer-service's Customer.customerNo. updatable=false is deliberate
    // (immutable once set) but it also means a plain entity save() can never write it
    // after the row exists, since Hibernate excludes non-updatable columns from
    // generated UPDATE statements — see LoanRepository.updateLoanNo, a bulk JPQL
    // update, which is the only way around that. Nullable (unlike customerNo) because
    // ddl-auto=update can't add a NOT NULL column to a table that already has rows;
    // LoanNoBackfill fills in rows that predate this fix.
    @Column(unique = true, updatable = false)
    private String loanNo;

    @Column(nullable = false)
    private Long customerId;

    private Long branchId;

    // Copied from Application.loanProductId when an approval creates this Loan (see
    // ApplicationServiceImpl.addApproval()). Nullable for the same ddl-auto=update reason
    // as loanNo above.
    @Column(name = "loan_product_id")
    private UUID loanProductId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    // Interpreted per termUnit: a literal month count for MONTH, a year count
    // for YEAR (multiplied by 12 at schedule-generation time), or a day count
    // for DAY (a single bullet repayment instead of a monthly schedule).
    @Column(nullable = false)
    private Integer termMonths;

    // Default backfills existing rows when Hibernate adds this NOT NULL column
    // to a non-empty table under ddl-auto=update.
    @Enumerated(EnumType.STRING)
    @Column(name = "term_unit", nullable = false, columnDefinition = "varchar(10) default 'MONTH'")
    @Builder.Default
    private TermUnit termUnit = TermUnit.MONTH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    private String purpose;

    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime disbursedAt;
    private LocalDateTime closedAt;

    private LocalDate maturityDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(precision = 15, scale = 2)
    private BigDecimal outstandingBalance;
}
