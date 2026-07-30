package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// One open/working case per loan that has (or had) overdue payments. Created
// lazily on first assign/status-change/note, not for every loan the workqueue
// happens to list — a loan with no case yet is simply "unassigned" in the
// workqueue response, not missing data.
@Entity
@Table(name = "collection_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionCase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long loanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CollectionCaseStatus status = CollectionCaseStatus.OPEN;

    private Long assignedToUserId;

    private LocalDateTime lastContactAt;

    private LocalDate nextFollowUpAt;
}
