package com.example.loan.repository;

import com.example.loan.entity.LoanPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPenaltyRepository extends JpaRepository<LoanPenalty, Long> {

    List<LoanPenalty> findByLoanIdOrderByAppliedDateAsc(Long loanId);
}
