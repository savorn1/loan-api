package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

// Directional: this row says `customer` has `relationshipType` to `relatedCustomer`
// (e.g. customer=PARENT of relatedCustomer) — not implicitly mirrored the other way.
@Entity
@Table(name = "customer_relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRelationship extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "related_customer_id", nullable = false)
    private Customer relatedCustomer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType;
}
