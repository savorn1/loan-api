package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_identities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerIdentity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentityType identityType;

    @Column(nullable = false)
    private String identityNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String issuingCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IdentityStatus status = IdentityStatus.ACTIVE;

    // Scanned copy of the physical document (passport photo page, ID card, etc.)
    @Column(name = "scan_file_name", length = 255)
    private String scanFileName;

    @Column(name = "scan_file_url", length = 500)
    private String scanFileUrl;
}
