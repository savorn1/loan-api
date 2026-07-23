package com.example.loan.repository;

import com.example.loan.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanIdOrderByPaymentDateAsc(Long loanId);
}
