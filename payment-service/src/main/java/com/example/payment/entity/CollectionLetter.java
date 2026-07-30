package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// A formal collection notice recorded against a case. This is a record-keeping
// entry, not a generation/delivery integration — status moves DRAFT -> SENT ->
// DELIVERED/FAILED as the collector updates it by hand.
@Entity
@Table(name = "collection_letters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionLetter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collection_case_id", nullable = false)
    private CollectionCase collectionCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LetterType letterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LetterDeliveryMethod deliveryMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LetterStatus status = LetterStatus.DRAFT;

    private String recipientAddress;

    @Column(columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private String generatedByName;

    private LocalDateTime sentAt;
}
