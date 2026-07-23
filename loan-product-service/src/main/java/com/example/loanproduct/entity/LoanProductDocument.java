package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_product_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProductDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_template_id", nullable = false)
    private DocumentTemplate documentTemplate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean required = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentTemplateStatus status = DocumentTemplateStatus.ACTIVE;
}
