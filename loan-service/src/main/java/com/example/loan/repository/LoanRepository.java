package com.example.loan.repository;

import com.example.loan.dto.BranchOutstandingRow;
import com.example.loan.dto.CohortStatusRow;
import com.example.loan.dto.CustomerOutstandingRow;
import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanPricingRow;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByLoanNoIsNull();

    // loanNo is @Column(updatable = false) so a normal entity save() silently excludes
    // it from the generated UPDATE — a bulk JPQL update bypasses that mapping
    // restriction, which is the only way to write it after the row already exists.
    @Modifying
    @Query("UPDATE Loan l SET l.loanNo = :loanNo WHERE l.id = :id")
    void updateLoanNo(@Param("id") Long id, @Param("loanNo") String loanNo);

    // COUNT/SUM with no GROUP BY always returns exactly one row (sums null
    // when there are zero matches) — safe as a singular (non-List) result.
    @Query("select new com.example.loan.dto.PortfolioSummaryResponse(count(l), sum(l.principal), sum(l.outstandingBalance)) " +
            "from Loan l where l.status = com.example.loan.entity.LoanStatus.ACTIVE")
    PortfolioSummaryResponse aggregatePortfolioSummary();

    // function('to_char', ...) has no statically-known Java return type on its
    // own, which makes Hibernate's `select new` constructor-matching fail
    // ("Missing constructor") even though the DTO's first param is a String —
    // the explicit cast gives it one.
    @Query("select new com.example.loan.dto.DisbursementTrendPointResponse(cast(function('to_char', l.disbursedAt, 'YYYY-MM') as string), count(l), sum(l.principal)) " +
            "from Loan l where l.disbursedAt >= :since " +
            "group by function('to_char', l.disbursedAt, 'YYYY-MM') " +
            "order by function('to_char', l.disbursedAt, 'YYYY-MM')")
    List<DisbursementTrendPointResponse> aggregateDisbursementTrend(@Param("since") LocalDateTime since);

    @Query("select new com.example.loan.dto.LoanStatusBreakdownResponse(l.status, count(l), sum(l.principal)) " +
            "from Loan l group by l.status")
    List<LoanStatusBreakdownResponse> aggregateStatusBreakdown();

    // disbursedAt is only set once a loan is actually disbursed (ACTIVE/CLOSED) —
    // PENDING/APPROVED/REJECTED loans never appear in a cohort, same reasoning as
    // aggregateDisbursementTrend.
    @Query("select new com.example.loan.dto.CohortStatusRow(cast(function('to_char', l.disbursedAt, 'YYYY-MM') as string), l.status, count(l), sum(l.principal)) " +
            "from Loan l where l.disbursedAt is not null and l.disbursedAt >= :since " +
            "group by function('to_char', l.disbursedAt, 'YYYY-MM'), l.status " +
            "order by function('to_char', l.disbursedAt, 'YYYY-MM')")
    List<CohortStatusRow> aggregateVintageCohorts(@Param("since") LocalDateTime since);

    @Query("select new com.example.loan.dto.BranchOutstandingRow(l.branchId, count(l), sum(l.outstandingBalance)) " +
            "from Loan l where l.status = com.example.loan.entity.LoanStatus.ACTIVE " +
            "group by l.branchId order by sum(l.outstandingBalance) desc")
    List<BranchOutstandingRow> aggregateOutstandingByBranch();

    @Query("select new com.example.loan.dto.CustomerOutstandingRow(l.customerId, count(l), sum(l.outstandingBalance)) " +
            "from Loan l where l.status = com.example.loan.entity.LoanStatus.ACTIVE " +
            "group by l.customerId order by sum(l.outstandingBalance) desc")
    List<CustomerOutstandingRow> aggregateOutstandingByCustomer();

    // Pricing report only needs loans that were actually priced and disbursed —
    // ACTIVE/CLOSED, same population as the vintage report.
    @Query("select new com.example.loan.dto.LoanPricingRow(l.interestRate, l.principal, l.termMonths) " +
            "from Loan l where l.status = com.example.loan.entity.LoanStatus.ACTIVE " +
            "or l.status = com.example.loan.entity.LoanStatus.CLOSED")
    List<LoanPricingRow> findPricingProjection();
}
