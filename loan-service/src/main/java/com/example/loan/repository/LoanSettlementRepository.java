package com.example.loan.repository;

import com.example.loan.entity.LoanSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanSettlementRepository extends JpaRepository<LoanSettlement, Long> {

    Optional<LoanSettlement> findByLoanId(Long loanId);
}
