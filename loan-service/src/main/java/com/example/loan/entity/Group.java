package com.example.loan.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    // Auto-generated from the id once persisted (see GroupServiceImpl.create) when not supplied —
    // null on the very first insert, so this can't be nullable=false like Group.name.
    @Column(unique = true, length = 50)
    private String code;

    // Cross-service id (loan-product-service) — raw/unvalidated, same convention as customerId/branchId.
    @Column(name = "loan_product_id")
    private UUID loanProductId;

    private Long branchId;

    private Long leaderCustomerId;

    @Column(name = "formation_date")
    private LocalDate formationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;
}
