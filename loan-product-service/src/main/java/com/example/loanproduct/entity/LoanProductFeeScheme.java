package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_product_fee_scheme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductFeeScheme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_scheme_id", nullable = false)
    private FeeScheme feeScheme;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    // Lower value = evaluated first when more than one assignment's window covers a given date.
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    // Nullable — no end date means the assignment is open-ended (still in effect).
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FeeSchemeStatus status = FeeSchemeStatus.ACTIVE;
}
