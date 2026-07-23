package com.example.accounting.repository;

import com.example.accounting.entity.TrialBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrialBalanceRepository extends JpaRepository<TrialBalance, Long> {

    List<TrialBalance> findByFinancialPeriodIdOrderByGeneratedAtDesc(Long financialPeriodId);
}
