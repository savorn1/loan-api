package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// One row per reconciliation check (see ReconciliationScheduler, daily) — lets the report
// show a trend ("matched until Aug 3, drifting since") instead of only the current instant.
// BaseEntity.createdAt is the check's timestamp; there's no separate column for it.
@Entity
@Table(name = "reconciliation_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationSnapshot extends BaseEntity {

    @Column(name = "gl_account_no", nullable = false, length = 20)
    private String glAccountNo;

    @Column(name = "gl_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal glBalance;

    @Column(name = "loan_service_outstanding_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal loanServiceOutstandingTotal;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal variance;

    @Column(nullable = false)
    private boolean matched;
}
