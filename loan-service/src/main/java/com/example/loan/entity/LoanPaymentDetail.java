package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// How much of one LoanPayment was applied to one LoanScheduleInstallment —
// a payment spanning several unpaid installments gets one row per
// installment it touched, split into its principal/interest components.
// Computed by LoanServiceImpl.addPayment(); never written directly.
@Entity
@Table(name = "loan_payment_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPaymentDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private LoanPayment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_installment_id", nullable = false)
    private LoanScheduleInstallment scheduleInstallment;

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "penalty_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal penaltyAmount = BigDecimal.ZERO;
}
