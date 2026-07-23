package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_product_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_template_id", nullable = false)
    private RuleTemplate ruleTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RuleTemplateStatus status = RuleTemplateStatus.ACTIVE;
}
