package com.example.accounting.repository;

import com.example.accounting.dto.AccountSideTotal;
import com.example.accounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    List<JournalEntryLine> findByJournalEntryIdOrderByLineNoAsc(Long journalEntryId);

    @Query("select l from JournalEntryLine l " +
            "where l.glAccount.id = :glAccountId and l.journalEntry.financialPeriod.id = :periodId " +
            "and l.journalEntry.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "order by l.journalEntry.transactionDate asc, l.lineNo asc")
    List<JournalEntryLine> findPostedLinesForAccountAndPeriod(@Param("glAccountId") Long glAccountId,
                                                                @Param("periodId") Long periodId);

    @Query("select new com.example.accounting.dto.AccountSideTotal(l.glAccount.id, l.entrySide, sum(l.amount)) " +
            "from JournalEntryLine l " +
            "where l.journalEntry.financialPeriod.id = :periodId " +
            "and l.journalEntry.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "group by l.glAccount.id, l.entrySide")
    List<AccountSideTotal> aggregateByAccountAndSideForPeriod(@Param("periodId") Long periodId);
}
