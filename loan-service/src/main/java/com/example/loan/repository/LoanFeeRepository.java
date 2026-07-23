package com.example.loan.repository;

import com.example.loan.entity.LoanFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanFeeRepository extends JpaRepository<LoanFee, Long> {

    List<LoanFee> findByLoanIdOrderByChargedDateAsc(Long loanId);
}
