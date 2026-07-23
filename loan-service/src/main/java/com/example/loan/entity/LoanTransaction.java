package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// The unified money-movement ledger for a loan — read-only, appended by
// LoanServiceImpl.recordTransaction() alongside every action that actually
// moves money (disbursement, payment allocation, penalty/fee payment,
// adjustment, write-off, settlement). referenceType/referenceId trace each
// row back to the record that caused it; balanceAfter is the loan's
// outstandingBalance immediately after this event, so the ledger can be
// read as a running balance without recomputing anything.
@Entity
@Table(name = "loan_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    private String description;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;
}
