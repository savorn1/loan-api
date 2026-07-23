package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// An asset pledged as security for the loan — a loan may have several, each
// released independently (typically once the loan is settled/closed).
@Entity
@Table(name = "loan_collaterals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanCollateral extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollateralType type;

    @Column(nullable = false)
    private String description;

    @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CollateralStatus status = CollateralStatus.PLEDGED;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;
}
