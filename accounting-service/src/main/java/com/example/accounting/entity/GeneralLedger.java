package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// One row per (gl_account, financial_period) — the maintained balance ledger, updated
// incrementally whenever a journal entry posts (see GeneralLedgerServiceImpl.applyPostedEntry)
// instead of being re-aggregated from journal_entry_lines on every read. openingBalance carries
// forward from the immediately preceding period's closingBalance the first time this account
// is touched in a new period (0 if there's no earlier period activity at all).
@Entity
@Table(name = "general_ledger", uniqueConstraints = @UniqueConstraint(columnNames = {"gl_account_id", "financial_period_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralLedger extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GlAccount glAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_period_id", nullable = false)
    private FinancialPeriod financialPeriod;

    // All four balances are signed in the account's own normalBalance direction.
    @Column(name = "opening_balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "period_debit_total", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal periodDebitTotal = BigDecimal.ZERO;

    @Column(name = "period_credit_total", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal periodCreditTotal = BigDecimal.ZERO;

    @Column(name = "closing_balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal closingBalance = BigDecimal.ZERO;
}
