package com.example.loan.repository;

import com.example.loan.entity.LoanScheduleInstallment;
import com.example.loan.entity.ScheduleInstallmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LoanScheduleInstallmentRepository extends JpaRepository<LoanScheduleInstallment, Long> {

    List<LoanScheduleInstallment> findByScheduleIdOrderByInstallmentNumberAsc(Long scheduleId);

    List<LoanScheduleInstallment> findByScheduleIdAndStatusNotOrderByInstallmentNumberAsc(
            Long scheduleId, ScheduleInstallmentStatus status);

    // Used by OverdueInstallmentScheduler — statuses is PENDING/PARTIALLY_PAID, i.e. still
    // unpaid; PAID and OVERDUE itself are excluded so already-flagged rows aren't reprocessed.
    List<LoanScheduleInstallment> findByDueDateBeforeAndStatusIn(
            LocalDate date, Collection<ScheduleInstallmentStatus> statuses);
}
