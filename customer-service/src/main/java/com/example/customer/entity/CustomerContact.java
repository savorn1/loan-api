package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String name;

    // Free text (e.g. "Spouse", "Employer", "Sibling") rather than an enum —
    // unlike identityType/addressType, the schema didn't list a fixed set of
    // relationship values.
    private String relationship;

    private String phone;
    private String email;
    private String address;
}
