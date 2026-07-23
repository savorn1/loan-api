package com.example.loanproduct.repository;

import com.example.loanproduct.entity.LoanProductFeeScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanProductFeeSchemeRepository extends JpaRepository<LoanProductFeeScheme, UUID> {

    List<LoanProductFeeScheme> findByLoanProductId(UUID loanProductId);

    Optional<LoanProductFeeScheme> findByIdAndLoanProductId(UUID id, UUID loanProductId);
}
