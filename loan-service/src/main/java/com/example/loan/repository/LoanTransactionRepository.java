package com.example.loan.repository;

import com.example.loan.entity.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {

    List<LoanTransaction> findByLoanIdOrderByTransactionDateAscIdAsc(Long loanId);
}
