package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    // The business event this template fires for — e.g. every DISBURSEMENT posts through
    // this template's lines, resolved to real GL accounts by an AccountingScheme.
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JournalTemplateStatus status = JournalTemplateStatus.ACTIVE;

    // Lines have no lifecycle of their own — they only ever exist as part of their template.
    @OneToMany(mappedBy = "journalTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNo asc")
    @Builder.Default
    private List<JournalTemplateLine> lines = new ArrayList<>();
}
