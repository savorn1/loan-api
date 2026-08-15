package com.example.loan.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupLoanApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assigned right after the initial save via GroupLoanApplicationRepository.updateApplicationNo
    // (a bulk JPQL update) rather than a second save() — see the comment on
    // Application.applicationNo for why a normal entity save() can't write an
    // updatable=false column once the row exists. Nullable because ddl-auto=update
    // can't add a NOT NULL column to a table that already has rows; GroupLoanApplicationNoBackfill
    // fills in rows that predate this column.
    @Column(unique = true, updatable = false)
    private String applicationNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // Copied from the group's branchId at creation — same unvalidated cross-service convention as Application.branchId.
    private Long branchId;

    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupLoanApplicationStatus status = GroupLoanApplicationStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
