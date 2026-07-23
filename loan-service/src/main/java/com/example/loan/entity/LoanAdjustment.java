package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// A manual balance correction — CREDIT reduces outstandingBalance (like an
// off-books payment or goodwill write-down), DEBIT increases it (e.g. a
// correction for an under-charged amount). Applied immediately on creation;
// no pending/approval workflow.
@Entity
@Table(name = "loan_adjustments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAdjustment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdjustmentType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;
}
