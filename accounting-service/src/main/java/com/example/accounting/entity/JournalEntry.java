package com.example.accounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry extends BaseEntity {

    // Assigned on save (JE-<id>, zero-padded) once the identity id is known — see
    // JournalEntryServiceImpl.create().
    @Column(name = "entry_no", unique = true, length = 20)
    private String entryNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_period_id", nullable = false)
    private FinancialPeriod financialPeriod;

    // Points back at the upstream domain object (e.g. a loan-service disbursement id) without
    // this service knowing what a "loan" is — just an opaque reference type + id pair.
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JournalEntryStatus status = JournalEntryStatus.DRAFT;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "posted_by", length = 100)
    private String postedBy;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNo asc")
    @Builder.Default
    private List<JournalEntryLine> lines = new ArrayList<>();
}
