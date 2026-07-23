package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "trial_balance_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalanceLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trial_balance_id", nullable = false)
    private TrialBalance trialBalance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GlAccount glAccount;

    @Column(name = "total_debit", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDebit;

    @Column(name = "total_credit", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCredit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;
}
