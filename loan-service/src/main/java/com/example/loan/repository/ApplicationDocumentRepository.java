package com.example.loan.repository;

import com.example.loan.entity.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findByApplicationIdOrderByUploadedAtAsc(Long applicationId);
}
