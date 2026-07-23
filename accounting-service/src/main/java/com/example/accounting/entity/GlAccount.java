package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gl_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GlAccount parent;

    @Column(name = "account_no", nullable = false, unique = true, length = 20)
    private String accountNo;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "normal_balance", nullable = false)
    private EntrySide normalBalance;

    @Column(nullable = false, length = 3)
    private String currency;

    // Summary/parent accounts in the hierarchy are usually not postable directly —
    // only their leaf accounts are.
    @Column(name = "allow_posting", nullable = false)
    @Builder.Default
    private boolean allowPosting = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GlAccountStatus status = GlAccountStatus.ACTIVE;
}
