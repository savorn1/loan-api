package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

// Read-only audit trail — one row is appended by CollectionServiceImpl every
// time a case is (re)assigned. assignedToUserId is null for an "unassigned"
// event; createdAt (from BaseEntity) doubles as the assigned-at timestamp.
@Entity
@Table(name = "collection_case_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionCaseAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_case_id", nullable = false)
    private CollectionCase collectionCase;

    private Long assignedToUserId;

    @Column(nullable = false)
    private String assignedBy;

    private String note;
}
