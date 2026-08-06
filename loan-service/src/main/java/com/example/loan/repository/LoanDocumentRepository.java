package com.example.loan.repository;

import com.example.loan.entity.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Long> {

    List<LoanDocument> findByLoanIdOrderByCreatedAtAsc(Long loanId);
}
