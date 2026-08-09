package com.example.loan.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cash recovered on a loan that was already written off (LoanWriteoff, COMPLETED) — the loan
// itself stays CLOSED; this doesn't reopen it, just tracks that some or all of the charged-off
// amount was later collected. See LoanServiceImpl.recordWriteoffRecovery.
@Entity
@Table(name = "loan_writeoff_recoveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanWriteoffRecovery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "writeoff_id", nullable = false)
    private LoanWriteoff writeoff;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "recovery_date", nullable = false)
    private LocalDate recoveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisbursementMethod method;

    private String reference;

    @Column(name = "created_by")
    private String createdBy;
}
