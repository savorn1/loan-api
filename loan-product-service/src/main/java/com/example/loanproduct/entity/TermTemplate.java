package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "term_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "term_value", nullable = false)
    private Integer termValue;

    // Default backfills existing rows when Hibernate adds this NOT NULL column
    // to a non-empty table under ddl-auto=update.
    @Enumerated(EnumType.STRING)
    @Column(name = "term_unit", nullable = false, columnDefinition = "varchar(10) default 'MONTH'")
    @Builder.Default
    private TermUnit termUnit = TermUnit.MONTH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TermTemplateStatus status = TermTemplateStatus.ACTIVE;
}
