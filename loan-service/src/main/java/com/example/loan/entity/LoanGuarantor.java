package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// A third party backing repayment of the loan — a loan may have several,
// each released independently (typically once the loan is settled/closed).
@Entity
@Table(name = "loan_guarantors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanGuarantor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String relationship;

    @Column(name = "guaranteed_amount", precision = 15, scale = 2)
    private BigDecimal guaranteedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GuarantorStatus status = GuarantorStatus.ACTIVE;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;
}
