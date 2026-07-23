package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// A late fee / charge applied to a loan — starts PENDING, then either PAID
// or WAIVED (terminal either way; no further transitions).
@Entity
@Table(name = "loan_penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPenalty extends BaseEntity {

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

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PenaltyStatus status = PenaltyStatus.PENDING;

    @Column(name = "waived_at")
    private LocalDateTime waivedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
