package com.example.accounting.repository;

import com.example.accounting.dto.AccountSideTotal;
import com.example.accounting.dto.BranchAccountTypeTotal;
import com.example.accounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("select l from JournalEntryLine l " +
            "where l.glAccount.id = :glAccountId " +
            "and l.journalEntry.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "and (cast(:dateFrom as date) is null or l.journalEntry.transactionDate >= :dateFrom) " +
            "and (cast(:dateTo as date) is null or l.journalEntry.transactionDate <= :dateTo) " +
            "order by l.journalEntry.transactionDate asc, l.lineNo asc")
    List<JournalEntryLine> findPostedLinesForAccountAndDateRange(@Param("glAccountId") Long glAccountId,
                                                                    @Param("dateFrom") LocalDate dateFrom,
                                                                    @Param("dateTo") LocalDate dateTo);

    @Query("select l from JournalEntryLine l " +
            "where l.glAccount.id = :glAccountId " +
            "and l.journalEntry.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "and l.journalEntry.transactionDate < :beforeDate " +
            "order by l.journalEntry.transactionDate asc, l.lineNo asc")
    List<JournalEntryLine> findPostedLinesForAccountBeforeDate(@Param("glAccountId") Long glAccountId,
                                                                  @Param("beforeDate") LocalDate beforeDate);

    @Query("select l from JournalEntryLine l " +
            "where l.journalEntry.branchId = :branchId " +
            "and l.journalEntry.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "and (cast(:dateFrom as date) is null or l.journalEntry.transactionDate >= :dateFrom) " +
            "and (cast(:dateTo as date) is null or l.journalEntry.transactionDate <= :dateTo) " +
            "order by l.journalEntry.transactionDate asc, l.lineNo asc")
    List<JournalEntryLine> findPostedLinesForBranchAndDateRange(@Param("branchId") Long branchId,
                                                                   @Param("dateFrom") LocalDate dateFrom,
                                                                   @Param("dateTo") LocalDate dateTo);

    @Query("select new com.example.accounting.dto.BranchAccountTypeTotal(l.journalEntry.branchId, ga.accountType, l.entrySide, sum(l.amount)) " +
            "from JournalEntryLine l join l.glAccount ga " +
            "where l.journalEntry.status = com.example.accounting.entity.JournalEntryStatus.POSTED " +
            "and ga.accountType in (com.example.accounting.entity.AccountType.INCOME, com.example.accounting.entity.AccountType.EXPENSE) " +
            "and (cast(:dateFrom as date) is null or l.journalEntry.transactionDate >= :dateFrom) " +
            "and (cast(:dateTo as date) is null or l.journalEntry.transactionDate <= :dateTo) " +
            "group by l.journalEntry.branchId, ga.accountType, l.entrySide")
    List<BranchAccountTypeTotal> aggregateIncomeExpenseByBranch(@Param("dateFrom") LocalDate dateFrom,
                                                                   @Param("dateTo") LocalDate dateTo);
}
