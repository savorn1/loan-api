package com.example.accounting.repository;

import com.example.accounting.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByFinancialPeriodId(Long financialPeriodId);
}
