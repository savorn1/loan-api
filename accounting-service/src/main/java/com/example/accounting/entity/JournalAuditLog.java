package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Append-only trail of who did what to a journal entry and when — separate from
// BaseEntity's createdAt/updatedAt, which only describe this row's own lifecycle,
// not the entry's. Never updated or deleted once written.
@Entity
@Table(name = "journal_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalAuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalAuditAction action;

    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(columnDefinition = "text")
    private String details;
}
