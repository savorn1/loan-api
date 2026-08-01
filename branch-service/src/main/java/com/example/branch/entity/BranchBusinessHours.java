package com.example.branch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

// A branch's weekly operating-hours schedule — at most one row per (branch, day
// of week), upserted by day rather than managed as an open-ended add/remove list
// (see BranchController's PUT .../business-hours/{dayOfWeek}). openingTime/
// closingTime stay null when isClosed is true.
@Entity
@Table(name = "branch_business_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchBusinessHours extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    private LocalTime openingTime;

    private LocalTime closingTime;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private boolean closed = false;
}
