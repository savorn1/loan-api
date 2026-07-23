package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// A generated, immutable snapshot of the trial balance report for one financial period —
// distinct from TrialBalanceService's live computed report (GET /trial-balance), which
// re-aggregates journal_entry_lines on every call and reflects whatever is posted *right now*.
// A snapshot instead freezes the numbers at the moment it was generated (see
// TrialBalanceSnapshotServiceImpl.generate(), sourced from general_ledger balances) so it stays
// stable for audit purposes even if more entries post afterward. Multiple snapshots can exist
// for the same period.
@Entity
@Table(name = "trial_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_period_id", nullable = false)
    private FinancialPeriod financialPeriod;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by", nullable = false, length = 100)
    private String generatedBy;

    @OneToMany(mappedBy = "trialBalance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrialBalanceLine> lines = new ArrayList<>();
}
