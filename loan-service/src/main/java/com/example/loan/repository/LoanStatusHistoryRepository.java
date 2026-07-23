package com.example.loan.repository;

import com.example.loan.entity.LoanStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanStatusHistoryRepository extends JpaRepository<LoanStatusHistory, Long> {

    List<LoanStatusHistory> findByLoanIdOrderByChangedAtAsc(Long loanId);
}
