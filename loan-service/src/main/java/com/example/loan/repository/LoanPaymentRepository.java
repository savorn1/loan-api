package com.example.loan.repository;

import com.example.loan.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanIdOrderByPaymentDateAsc(Long loanId);

    List<LoanPayment> findByPaymentNoIsNull();

    // paymentNo is @Column(updatable = false) so a normal entity save() silently excludes
    // it from the generated UPDATE — a bulk JPQL update bypasses that mapping restriction,
    // which is the only way to write it after the row already exists.
    @Modifying
    @Query("UPDATE LoanPayment p SET p.paymentNo = :paymentNo WHERE p.id = :id")
    void updatePaymentNo(@Param("id") Long id, @Param("paymentNo") String paymentNo);
}
