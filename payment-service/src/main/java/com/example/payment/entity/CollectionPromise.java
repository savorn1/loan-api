package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// A promise-to-pay taken during a collections contact. Starts PENDING and is
// later resolved (KEPT/BROKEN/PARTIAL) once the promised date has passed.
@Entity
@Table(name = "collection_promises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPromise extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_case_id", nullable = false)
    private CollectionCase collectionCase;

    @Column(nullable = false)
    private BigDecimal promisedAmount;

    @Column(nullable = false)
    private LocalDate promisedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromiseStatus status = PromiseStatus.PENDING;

    private BigDecimal amountPaid;

    private String notes;

    @Column(nullable = false)
    private String createdByName;

    private LocalDateTime resolvedAt;
}
