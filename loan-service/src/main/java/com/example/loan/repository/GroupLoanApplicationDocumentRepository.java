package com.example.loan.repository;

import com.example.loan.entity.GroupLoanApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupLoanApplicationDocumentRepository extends JpaRepository<GroupLoanApplicationDocument, Long> {

    List<GroupLoanApplicationDocument> findByApplicationIdOrderByUploadedAtAsc(Long applicationId);
}
