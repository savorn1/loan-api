package com.example.loan.repository;

import com.example.loan.entity.LoanWriteoffRecovery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanWriteoffRecoveryRepository extends JpaRepository<LoanWriteoffRecovery, Long> {

    List<LoanWriteoffRecovery> findByWriteoffIdOrderByRecoveryDateAsc(Long writeoffId);
}
