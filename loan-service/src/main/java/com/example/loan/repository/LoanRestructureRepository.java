package com.example.loan.repository;

import com.example.loan.entity.LoanRestructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRestructureRepository extends JpaRepository<LoanRestructure, Long> {

    List<LoanRestructure> findByLoanIdOrderByEffectiveDateAsc(Long loanId);
}
