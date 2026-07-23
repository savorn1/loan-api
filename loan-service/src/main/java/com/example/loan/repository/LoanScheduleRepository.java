package com.example.loan.repository;

import com.example.loan.entity.LoanSchedule;
import com.example.loan.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {

    List<LoanSchedule> findByLoanIdOrderByGeneratedAtDesc(Long loanId);

    List<LoanSchedule> findByLoanIdAndStatus(Long loanId, ScheduleStatus status);
}
