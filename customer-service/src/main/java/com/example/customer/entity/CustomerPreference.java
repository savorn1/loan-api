package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

// One preferences record per customer — enforced by the unique customer_id
// join column, not a repeatable child list (same shape as CustomerRiskProfile).
@Entity
@Table(name = "customer_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    private String language;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationMethod notificationMethod;
}
