package com.example.loan.repository;

import com.example.loan.entity.LoanAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanAdjustmentRepository extends JpaRepository<LoanAdjustment, Long> {

    List<LoanAdjustment> findByLoanIdOrderByCreatedAtAsc(Long loanId);
}
