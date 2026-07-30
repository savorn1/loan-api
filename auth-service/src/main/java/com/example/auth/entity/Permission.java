package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;

// A fine-grained capability, identified by the (module, action) pair (e.g. module
// "LOAN", action "APPROVE") that can be attached to one or more RbacRoles via the
// role_permission join table. Nothing in the platform enforces these yet — this is
// the data model for a future permission-based authorization layer, managed today
// purely as reference data.
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Composed identifier, e.g. "LOAN_READ" — kept in sync with module/action by
    // syncName() below rather than settable directly, so it can never drift.
    @Column(nullable = false, unique = true)
    private String name;

    // The resource/domain this permission governs, e.g. "LOAN", "USER", "PAYMENT".
    @Column(nullable = false)
    private String module;

    // The operation on that module, e.g. "READ", "CREATE", "UPDATE", "DELETE".
    @Column(nullable = false)
    private String action;

    private String description;

    @PrePersist
    @PreUpdate
    private void syncName() {
        this.name = module + "_" + action;
    }
}
