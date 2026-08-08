package com.example.accounting.repository;

import com.example.accounting.entity.JournalEntry;
import com.example.accounting.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByFinancialPeriodId(Long financialPeriodId);

    // Idempotency check for JournalEntryServiceImpl.generate() — lets a retried Feign call
    // from loan-service (e.g. after a timeout) return the entry already created instead of
    // posting a duplicate. Not used by the manual create() flow, where referenceType/
    // referenceId are optional and reuse across entries is legitimate.
    Optional<JournalEntry> findByTransactionTypeAndReferenceTypeAndReferenceId(
            TransactionType transactionType, String referenceType, String referenceId);
}
