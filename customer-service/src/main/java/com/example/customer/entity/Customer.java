package com.example.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assigned after the initial save, once the id is known — see
    // CustomerServiceImpl.generateCustomerNo() (same two-phase-save pattern as
    // accounting-service's JournalEntry.entryNo).
    @Column(nullable = false, unique = true, updatable = false)
    private String customerNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType;

    // Required for INDIVIDUAL, left null for CORPORATE (which only carries a
    // fullName — the registered company/legal name).
    private String firstName;

    private String lastName;

    // Always populated: derived as "firstName lastName" for INDIVIDUAL,
    // supplied directly (company name) for CORPORATE.
    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(unique = true)
    private String nationalId;

    private String address;

    private LocalDate dateOfBirth;

    private String nationality;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    // Raw FK to a branch — no Branch entity/service exists in this system yet,
    // so this stays an unvalidated id (same pattern as referenceId/loanProductId
    // elsewhere in the platform: cross-service ids aren't enforced as JPA joins).
    private Long branchId;
}
