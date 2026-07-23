package com.example.loan.repository;

import com.example.loan.entity.LoanScheduleInstallment;
import com.example.loan.entity.ScheduleInstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanScheduleInstallmentRepository extends JpaRepository<LoanScheduleInstallment, Long> {

    List<LoanScheduleInstallment> findByScheduleIdOrderByInstallmentNumberAsc(Long scheduleId);

    List<LoanScheduleInstallment> findByScheduleIdAndStatusNotOrderByInstallmentNumberAsc(
            Long scheduleId, ScheduleInstallmentStatus status);
}
