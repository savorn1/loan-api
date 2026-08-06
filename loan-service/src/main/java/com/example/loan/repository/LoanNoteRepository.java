package com.example.loan.repository;

import com.example.loan.entity.LoanNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanNoteRepository extends JpaRepository<LoanNote, Long> {

    List<LoanNote> findByLoanIdOrderByCreatedAtAsc(Long loanId);
}
