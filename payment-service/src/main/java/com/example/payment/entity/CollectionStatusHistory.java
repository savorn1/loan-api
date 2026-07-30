package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

// Read-only audit trail — one row is appended by CollectionServiceImpl on every
// case status transition. No update/delete; createdAt (from BaseEntity) doubles
// as the changed-at timestamp.
@Entity
@Table(name = "collection_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionStatusHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_case_id", nullable = false)
    private CollectionCase collectionCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private CollectionCaseStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private CollectionCaseStatus toStatus;

    @Column(nullable = false)
    private String changedBy;

    private String note;
}
