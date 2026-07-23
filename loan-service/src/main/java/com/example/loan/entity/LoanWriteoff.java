package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Uncollectable debt record — at most one per loan (enforced in
// LoanServiceImpl, not a DB constraint), mirrors LoanSettlement's
// PENDING/COMPLETED shape. Completing it closes the loan and zeroes its
// outstandingBalance, same as completing a settlement.
@Entity
@Table(name = "loan_writeoffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanWriteoff extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Column(name = "writeoff_date", nullable = false)
    private LocalDate writeoffDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WriteoffStatus status = WriteoffStatus.PENDING;
}
