package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

// One profile per customer — enforced by the unique customer_id join column,
// not a repeatable child list like CustomerIdentity/CustomerAddress/etc.
@Entity
@Table(name = "customer_risk_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRiskProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    // Politically Exposed Person. Neither this nor sanctionChecked is
    // "is"-prefixed, so the primitive boolean's Lombok-generated isXxx()
    // getter still round-trips through Jackson as "pep"/"sanctionChecked" —
    // no boxing needed (contrast CustomerAddress.isPrimary, which is boxed).
    @Column(nullable = false)
    private boolean pep;

    @Column(nullable = false)
    private boolean sanctionChecked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmlStatus amlStatus;

    private LocalDate lastReviewDate;
}
