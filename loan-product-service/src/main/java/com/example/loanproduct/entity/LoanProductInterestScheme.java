package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_product_interest_scheme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductInterestScheme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_scheme_id", nullable = false)
    private InterestScheme interestScheme;

    // Lower value = evaluated first when more than one assignment's window covers a given date.
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    // Nullable — no end date means the assignment is open-ended (still in effect).
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InterestRateStatus status = InterestRateStatus.ACTIVE;
}
