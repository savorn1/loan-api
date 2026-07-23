package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rule_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleField field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleOperator operator;

    // Single comparison value; for BETWEEN this is the lower bound, for IN a comma-separated list.
    @Column(nullable = false)
    private String value;

    // Upper bound — only used (and required) when operator is BETWEEN.
    private String value2;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RuleTemplateStatus status = RuleTemplateStatus.ACTIVE;
}
