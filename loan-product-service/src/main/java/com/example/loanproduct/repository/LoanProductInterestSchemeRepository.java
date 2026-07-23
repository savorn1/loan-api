package com.example.loanproduct.repository;

import com.example.loanproduct.entity.LoanProductInterestScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanProductInterestSchemeRepository extends JpaRepository<LoanProductInterestScheme, UUID> {

    List<LoanProductInterestScheme> findByLoanProductId(UUID loanProductId);

    Optional<LoanProductInterestScheme> findByIdAndLoanProductId(UUID id, UUID loanProductId);

    List<LoanProductInterestScheme> findByLoanProductIdAndIsDefaultTrue(UUID loanProductId);

    boolean existsByLoanProductId(UUID loanProductId);
}
