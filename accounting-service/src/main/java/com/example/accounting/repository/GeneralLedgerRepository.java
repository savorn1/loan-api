package com.example.accounting.repository;

import com.example.accounting.entity.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {

    Optional<GeneralLedger> findByGlAccountIdAndFinancialPeriodId(Long glAccountId, Long financialPeriodId);

    List<GeneralLedger> findByFinancialPeriodId(Long financialPeriodId);

    // Rolls an account's balance forward: the most recent period (by end date) strictly
    // before the given period's start date that already has a ledger row for this account.
    Optional<GeneralLedger> findFirstByGlAccountIdAndFinancialPeriod_EndDateLessThanOrderByFinancialPeriod_EndDateDesc(
            Long glAccountId, LocalDate periodStartDate);
}
