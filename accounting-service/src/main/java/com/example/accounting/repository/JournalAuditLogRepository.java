package com.example.accounting.repository;

import com.example.accounting.entity.JournalAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalAuditLogRepository extends JpaRepository<JournalAuditLog, Long> {

    List<JournalAuditLog> findByJournalEntryIdOrderByPerformedAtAsc(Long journalEntryId);
}
