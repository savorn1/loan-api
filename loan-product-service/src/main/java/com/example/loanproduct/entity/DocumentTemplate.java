package com.example.loanproduct.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "document_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentTemplateStatus status = DocumentTemplateStatus.ACTIVE;

    // Optional reference file (e.g. an example of a valid scan) admins attach to
    // this document type so applicants know what to submit — not a per-applicant
    // upload, just one sample per template.
    @Column(name = "sample_file_name", length = 255)
    private String sampleFileName;

    @Column(name = "sample_file_url", length = 500)
    private String sampleFileUrl;
}
