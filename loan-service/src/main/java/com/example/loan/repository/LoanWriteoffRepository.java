package com.example.loan.repository;

import com.example.loan.entity.LoanWriteoff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanWriteoffRepository extends JpaRepository<LoanWriteoff, Long> {

    Optional<LoanWriteoff> findByLoanId(Long loanId);
}
