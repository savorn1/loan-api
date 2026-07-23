package com.example.accounting.repository;

import com.example.accounting.entity.FinancialPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface FinancialPeriodRepository extends JpaRepository<FinancialPeriod, Long> {

    boolean existsByPeriodName(String periodName);

    @Query("select p from FinancialPeriod p where :date between p.startDate and p.endDate")
    Optional<FinancialPeriod> findByDateWithinRange(@Param("date") LocalDate date);
}
