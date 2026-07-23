package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

// Binds a JournalTemplate's symbolic account role (e.g. "CASH") to a real GlAccount,
// per currency. This is the layer of indirection that keeps JournalTemplate reusable
// across currencies/books without hardcoding GL account numbers into the template itself.
@Entity
@Table(name = "accounting_schemes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"journal_template_id", "account_role", "currency"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingScheme extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_template_id", nullable = false)
    private JournalTemplate journalTemplate;

    @Column(name = "account_role", nullable = false, length = 50)
    private String accountRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GlAccount glAccount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountingSchemeStatus status = AccountingSchemeStatus.ACTIVE;
}
