package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
}
