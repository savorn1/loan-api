package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Final closure record — at most one per loan (enforced in LoanServiceImpl,
// not a DB constraint). Completing it closes the loan: outstandingBalance is
// zeroed and status moves to CLOSED, regardless of settlementAmount (a
// negotiated payoff is typically less than the full outstanding balance).
@Entity
@Table(name = "loan_settlements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "settlement_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    private String note;
}
