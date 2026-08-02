package com.example.loan.repository;

import com.example.loan.entity.LoanRefinance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRefinanceRepository extends JpaRepository<LoanRefinance, Long> {

    List<LoanRefinance> findByLoanIdOrderByEffectiveDateAsc(Long loanId);
}
