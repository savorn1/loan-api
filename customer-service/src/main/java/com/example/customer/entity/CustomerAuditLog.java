package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Deliberately doesn't extend BaseEntity: an audit log entry is append-only —
// it has no updatedAt (nothing should ever modify one) and no deletedAt
// (nothing should ever soft-delete one). Nothing currently writes rows here;
// this is the read side only, ready for a future instrumented write path.
@Entity
@Table(name = "customer_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Free text (e.g. "CUSTOMER_UPDATED", "IDENTITY_ADDED") rather than an
    // enum — the set of loggable actions will grow as write paths are
    // instrumented, so it isn't fixed yet.
    @Column(nullable = false)
    private String action;

    // Raw FK to a user in auth-service — no User entity in this service, same
    // cross-service-id pattern as Customer.branchId.
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime createdAt;
}
