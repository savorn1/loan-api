package com.example.loan.repository;

import com.example.loan.entity.LoanDisbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, Long> {

    List<LoanDisbursement> findByLoanIdOrderByDisbursedDateAsc(Long loanId);
}
