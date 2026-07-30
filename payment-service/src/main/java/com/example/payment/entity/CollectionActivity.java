package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "collection_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_case_id", nullable = false)
    private CollectionCase collectionCase;

    @Column(nullable = false)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMethod contactMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactOutcome outcome;

    @Column(nullable = false, columnDefinition = "text")
    private String note;

    private LocalDate followUpDate;
}
