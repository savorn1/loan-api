package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Document metadata only (name, type, an external URL/reference) — no file storage,
// matching loan-product-service's DocumentTemplate convention.
@Entity
@Table(name = "loan_application_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
