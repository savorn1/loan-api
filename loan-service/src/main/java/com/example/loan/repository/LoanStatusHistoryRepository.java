package com.example.loan.repository;

import com.example.loan.entity.LoanStatus;
import com.example.loan.entity.LoanStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanStatusHistoryRepository extends JpaRepository<LoanStatusHistory, Long> {

    List<LoanStatusHistory> findByLoanIdOrderByChangedAtAsc(Long loanId);

    // Cross-loan audit trail report — dateFrom/dateTo/status are all optional filters.
    // The casts are required: when a param is bound as null with no other type hint,
    // Postgres can't infer its type ("could not determine data type of parameter $1"),
    // same reasoning as accounting-service's JournalEntryLineRepository date-range queries.
    @Query("select h from LoanStatusHistory h " +
            "where (cast(:dateFrom as timestamp) is null or h.changedAt >= :dateFrom) " +
            "and (cast(:dateTo as timestamp) is null or h.changedAt <= :dateTo) " +
            "and (cast(:status as string) is null or h.toStatus = :status) " +
            "order by h.changedAt desc")
    List<LoanStatusHistory> findAuditTrail(@Param("dateFrom") LocalDateTime dateFrom,
                                            @Param("dateTo") LocalDateTime dateTo,
                                            @Param("status") LoanStatus status);
}
