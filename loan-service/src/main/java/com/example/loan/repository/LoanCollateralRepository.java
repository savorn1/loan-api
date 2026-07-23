package com.example.loan.repository;

import com.example.loan.entity.LoanCollateral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanCollateralRepository extends JpaRepository<LoanCollateral, Long> {

    List<LoanCollateral> findByLoanIdOrderByCreatedAtAsc(Long loanId);
}
