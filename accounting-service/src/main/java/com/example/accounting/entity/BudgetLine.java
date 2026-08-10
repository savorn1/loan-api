package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// A manually-entered budget figure for one GL account in one financial period —
// company-wide, not branch-scoped (branch-level budgeting would need this service to
// validate branchId against branch-service, which nothing here currently does; every
// other raw branchId field in this codebase is left unvalidated by convention, but an
// unvalidated FK on money someone is explicitly typing in is worse than not offering
// the field yet). Compared against actual GL activity by BudgetVsActualReport.
@Entity
@Table(name = "budget_lines", uniqueConstraints = @UniqueConstraint(columnNames = {"financial_period_id", "gl_account_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_period_id", nullable = false)
    private FinancialPeriod financialPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GlAccount glAccount;

    @Column(name = "budget_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal budgetAmount;
}
