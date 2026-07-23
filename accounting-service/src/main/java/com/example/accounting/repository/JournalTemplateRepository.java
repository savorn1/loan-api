package com.example.accounting.repository;

import com.example.accounting.entity.JournalTemplate;
import com.example.accounting.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JournalTemplateRepository extends JpaRepository<JournalTemplate, Long> {

    boolean existsByCode(String code);

    Optional<JournalTemplate> findByCode(String code);

    List<JournalTemplate> findByTransactionType(TransactionType transactionType);
}
