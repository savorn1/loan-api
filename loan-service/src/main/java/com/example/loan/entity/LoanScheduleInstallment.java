package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// One installment line of a LoanSchedule — the amortization breakdown for a
// single due date (AmortizationCalculator's output, persisted).
@Entity
@Table(name = "loan_schedule_installments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanScheduleInstallment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private LoanSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "outstanding_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleInstallmentStatus status = ScheduleInstallmentStatus.PENDING;
}
