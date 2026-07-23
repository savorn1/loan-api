package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// One amortization run — system-generated on disbursement (and, in future, on
// each restructure). At most one is ACTIVE per loan at a time; regenerating
// flips the previous ACTIVE run to SUPERSEDED rather than deleting it, so the
// prior installment breakdown stays on record.
@Entity
@Table(name = "loan_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "total_installments", nullable = false)
    private Integer totalInstallments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.ACTIVE;
}
