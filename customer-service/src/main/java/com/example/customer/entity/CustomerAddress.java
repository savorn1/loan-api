package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    private String country;
    private String province;
    private String district;
    private String commune;
    private String village;
    private String street;
    private String postalCode;

    // At most one address per customer has this set — enforced in
    // CustomerServiceImpl (clears the flag on the customer's other addresses)
    // rather than at the DB level.
    @Column(nullable = false)
    @Builder.Default
    private boolean isPrimary = false;
}
