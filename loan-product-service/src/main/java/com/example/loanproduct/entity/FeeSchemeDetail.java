package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fee_scheme_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeSchemeDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_scheme_id", nullable = false)
    private FeeScheme feeScheme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false)
    private FeeCalculationMethod calculationMethod;

    // Flat currency amount when calculationMethod is FLAT, percentage points (0-100) when PERCENTAGE.
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_timing", nullable = false)
    private FeeChargeTiming chargeTiming;
}
