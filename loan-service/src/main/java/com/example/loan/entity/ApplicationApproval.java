package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// An audit trail of every decision recorded against an application — appended to, never
// edited. approvedAmount/approvedInterestRate/approvedTermMonths are only populated when
// decision is APPROVED (underwriting can approve a different amount/rate/term than requested);
// ApplicationServiceImpl uses them to create the actual Loan.
@Entity
@Table(name = "loan_application_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationApproval extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "approver_name", nullable = false, length = 100)
    private String approverName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalDecision decision;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "approved_interest_rate", precision = 5, scale = 2)
    private BigDecimal approvedInterestRate;

    @Column(name = "approved_term_months")
    private Integer approvedTermMonths;

    @Column(columnDefinition = "text")
    private String comments;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;
}
