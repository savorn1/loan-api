package com.example.accounting.repository;

import com.example.accounting.entity.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetLineRepository extends JpaRepository<BudgetLine, Long> {

    List<BudgetLine> findByFinancialPeriodId(Long financialPeriodId);

    Optional<BudgetLine> findByFinancialPeriodIdAndGlAccountId(Long financialPeriodId, Long glAccountId);

    boolean existsByFinancialPeriodIdAndGlAccountId(Long financialPeriodId, Long glAccountId);
}
