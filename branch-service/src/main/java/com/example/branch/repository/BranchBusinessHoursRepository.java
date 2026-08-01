package com.example.branch.repository;

import com.example.branch.entity.BranchBusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface BranchBusinessHoursRepository extends JpaRepository<BranchBusinessHours, Long> {

    Optional<BranchBusinessHours> findByBranch_IdAndDayOfWeek(Long branchId, DayOfWeek dayOfWeek);

    List<BranchBusinessHours> findByBranch_Id(Long branchId);
}
