package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "interest_scheme_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestSchemeDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_scheme_id", nullable = false)
    private InterestScheme interestScheme;

    // Interpreted using the assigned product's termUnit (DAY/MONTH/YEAR), same as the old LoanProductInterestRate.
    @Column(name = "min_term", nullable = false)
    private Integer minTerm;

    @Column(name = "max_term", nullable = false)
    private Integer maxTerm;

    @Column(name = "min_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
}
