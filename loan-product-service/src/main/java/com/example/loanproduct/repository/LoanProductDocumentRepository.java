package com.example.loanproduct.repository;

import com.example.loanproduct.entity.LoanProductDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanProductDocumentRepository extends JpaRepository<LoanProductDocument, Long> {

    List<LoanProductDocument> findByLoanProductId(UUID loanProductId);

    Optional<LoanProductDocument> findByIdAndLoanProductId(Long id, UUID loanProductId);
}
