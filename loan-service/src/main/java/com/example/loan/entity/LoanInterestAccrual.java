package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// A manually recorded interest accrual entry for a period on the loan —
// append-only, no status; accruedAt is stamped server-side at creation.
@Entity
@Table(name = "loan_interest_accruals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanInterestAccrual extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "accrued_at", nullable = false)
    private LocalDateTime accruedAt;
}
