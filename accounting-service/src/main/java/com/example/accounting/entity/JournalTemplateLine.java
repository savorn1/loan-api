package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journal_template_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalTemplateLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_template_id", nullable = false)
    private JournalTemplate journalTemplate;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    // Symbolic placeholder (e.g. "CASH", "LOAN_RECEIVABLE", "INTEREST_INCOME") — an
    // AccountingScheme binds this role to an actual gl_account for a given currency.
    @Column(name = "account_role", nullable = false, length = 50)
    private String accountRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_side", nullable = false)
    private EntrySide entrySide;

    @Column(length = 255)
    private String description;
}
