package com.example.loan.repository;

import com.example.loan.entity.LoanInterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanInterestAccrualRepository extends JpaRepository<LoanInterestAccrual, Long> {

    List<LoanInterestAccrual> findByLoanIdOrderByPeriodStartAsc(Long loanId);
}
