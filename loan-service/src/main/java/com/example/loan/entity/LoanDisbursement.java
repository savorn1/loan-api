package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// A loan may be released in stages — each tranche is logged here. Distinct
// from Loan.disburse(), which flips the loan APPROVED -> ACTIVE and generates
// the repayment schedule in one shot; this is just the funds-released ledger.
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
}
