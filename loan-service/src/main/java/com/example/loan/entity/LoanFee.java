package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// A charge applied to this specific loan instance (processing, insurance,
// administration, etc.) — distinct from loan-product-service's FeeScheme,
// which defines a product's fee template rather than a charge on one loan.
// Starts PENDING, then either PAID or WAIVED.
@Entity
@Table(name = "loan_fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanFee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanFeeType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "charged_date", nullable = false)
    private LocalDate chargedDate;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FeeStatus status = FeeStatus.PENDING;

    @Column(name = "waived_at")
    private LocalDateTime waivedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
