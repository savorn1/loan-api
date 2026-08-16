package com.example.loan.repository;

import com.example.loan.entity.LoanDisbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, Long> {

    List<LoanDisbursement> findByLoanIdOrderByDisbursedDateAsc(Long loanId);

    List<LoanDisbursement> findByDisbursementNoIsNull();

    // disbursementNo is @Column(updatable = false) so a normal entity save() silently
    // excludes it from the generated UPDATE — a bulk JPQL update bypasses that mapping
    // restriction, which is the only way to write it after the row already exists.
    @Modifying
    @Query("UPDATE LoanDisbursement d SET d.disbursementNo = :disbursementNo WHERE d.id = :id")
    void updateDisbursementNo(@Param("id") Long id, @Param("disbursementNo") String disbursementNo);
}
