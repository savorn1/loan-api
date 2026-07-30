package com.example.payment.repository;

import com.example.payment.dto.CollectionTrendPointResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByLoanId(Long loanId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByDueDateBeforeAndStatus(LocalDate date, PaymentStatus status);

    List<Payment> findByDueDateAndStatus(LocalDate date, PaymentStatus status);

    // See loan-service's LoanRepository.aggregateDisbursementTrend for why the
    // cast is required (function()'s return type is otherwise unresolved,
    // which breaks Hibernate's `select new` constructor-matching).
    @Query("select new com.example.payment.dto.CollectionTrendPointResponse(cast(function('to_char', p.paidAt, 'YYYY-MM') as string), count(p), sum(p.amount)) " +
            "from Payment p where p.status = com.example.payment.entity.PaymentStatus.PAID and p.paidAt >= :since " +
            "group by function('to_char', p.paidAt, 'YYYY-MM') " +
            "order by function('to_char', p.paidAt, 'YYYY-MM')")
    List<CollectionTrendPointResponse> aggregateCollectionTrend(@Param("since") LocalDate since);
}
