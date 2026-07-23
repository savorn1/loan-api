package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Read-only audit trail — one row is appended by LoanServiceImpl on every
// status transition (create/approve/reject/disburse/close). No request DTO —
// nothing ever creates or edits this directly.
@Entity
@Table(name = "loan_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private LoanStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private LoanStatus toStatus;

    private String note;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
