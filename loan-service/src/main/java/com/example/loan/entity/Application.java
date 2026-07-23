package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// The pre-approval workflow record (loan_applications) — deliberately a separate table from
// Loan (loans), not a status on it. A customer submits an Application; once an
// ApplicationApproval with decision APPROVED is recorded (see ApplicationServiceImpl), this
// service creates the actual Loan (already APPROVED, ready for the existing disburse() flow)
// and links it back here via loanId. Named "Application" rather than "LoanApplication" to
// avoid colliding with com.example.loan.LoanApplication, this module's Spring Boot bootstrap class.
@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "requested_term_months", nullable = false)
    private Integer requestedTermMonths;

    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    // Set once an approval decision creates the actual Loan record.
    @Column(name = "loan_id")
    private Long loanId;
}
