package com.example.loanproduct.repository;

import com.example.loanproduct.entity.LoanProductTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanProductTermRepository extends JpaRepository<LoanProductTerm, Long> {

    List<LoanProductTerm> findByLoanProductId(UUID loanProductId);

    Optional<LoanProductTerm> findByIdAndLoanProductId(Long id, UUID loanProductId);

    List<LoanProductTerm> findByLoanProductIdAndIsDefaultTrue(UUID loanProductId);

    boolean existsByLoanProductId(UUID loanProductId);
}
