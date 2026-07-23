package com.example.loanproduct.repository;

import com.example.loanproduct.entity.LoanProductRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanProductRuleRepository extends JpaRepository<LoanProductRule, Long> {

    List<LoanProductRule> findByLoanProductId(UUID loanProductId);

    Optional<LoanProductRule> findByIdAndLoanProductId(Long id, UUID loanProductId);
}
