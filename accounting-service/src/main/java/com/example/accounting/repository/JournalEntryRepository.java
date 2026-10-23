package com.example.accounting.repository;

import com.example.accounting.entity.JournalEntry;
import com.example.accounting.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByFinancialPeriodId(Long financialPeriodId);

    // Every posted entry in the window that moved one of the cash accounts, with its
    // lines fetched — the cash flow statement needs the *whole* entry, not just the
    // cash line, since the non-cash lines are what name the source or use of the cash.
    @Query("select distinct e from JournalEntry e " +
            "join fetch e.lines line " +
            "join fetch line.glAccount " +
            "where e.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "and e.transactionDate >= :dateFrom and e.transactionDate <= :dateTo " +
            "and exists (select 1 from JournalEntryLine cl " +
            "            where cl.journalEntry = e and cl.glAccount.accountNo in :cashAccountNos)")
    List<JournalEntry> findPostedEntriesTouchingCashAccounts(@Param("cashAccountNos") List<String> cashAccountNos,
                                                                @Param("dateFrom") LocalDate dateFrom,
                                                                @Param("dateTo") LocalDate dateTo);

    // Idempotency check for JournalEntryServiceImpl.generate() — lets a retried Feign call
    // from loan-service (e.g. after a timeout) return the entry already created instead of
    // posting a duplicate. Not used by the manual create() flow, where referenceType/
    // referenceId are optional and reuse across entries is legitimate.
    Optional<JournalEntry> findByTransactionTypeAndReferenceTypeAndReferenceId(
            TransactionType transactionType, String referenceType, String referenceId);
}
