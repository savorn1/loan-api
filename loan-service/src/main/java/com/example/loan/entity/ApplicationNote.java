package com.example.loan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_application_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(nullable = false, columnDefinition = "text")
    private String note;
}
