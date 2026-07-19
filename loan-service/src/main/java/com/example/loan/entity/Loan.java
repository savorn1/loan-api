package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.PENDING;

    private String purpose;

    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime disbursedAt;
    private LocalDateTime closedAt;

    private LocalDate maturityDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(precision = 15, scale = 2)
    private BigDecimal outstandingBalance;
}
