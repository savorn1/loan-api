package com.example.branch.entity;

import jakarta.persistence.*;
import lombok.*;

// A physical branch/office. Its own microservice (own DB) — auth-service (staff),
// customer-service (customers) and loan-service (loans/applications) each reference
// a branch only by its raw Long id, the same unvalidated-cross-service-id convention
// already used for customerId/loanProductId elsewhere in the platform. auth-service's
// UserServiceImpl calls this service's API (via BranchClient) to validate an id exists
// and to resolve a display name.
@Entity
@Table(name = "branch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stable machine identifier, e.g. "PP01" — separate from the human-facing name.
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String address;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BranchStatus status = BranchStatus.ACTIVE;
}
