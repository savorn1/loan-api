package com.example.loan.repository;

import com.example.loan.entity.LoanGuarantor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanGuarantorRepository extends JpaRepository<LoanGuarantor, Long> {

    List<LoanGuarantor> findByLoanIdOrderByCreatedAtAsc(Long loanId);
}
