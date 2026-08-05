package com.example.loan.repository;

import com.example.loan.dto.DisbursementTrendPointResponse;
import com.example.loan.dto.LoanStatusBreakdownResponse;
import com.example.loan.dto.PortfolioSummaryResponse;
import com.example.loan.entity.Loan;
import com.example.loan.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    List<Loan> findByCustomerId(Long customerId);

    List<Loan> findByStatus(LoanStatus status);

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
}
