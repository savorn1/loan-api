package com.example.payment.repository;

import com.example.payment.entity.CollectionCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollectionCaseRepository extends JpaRepository<CollectionCase, Long> {

    Optional<CollectionCase> findByLoanId(Long loanId);
}
