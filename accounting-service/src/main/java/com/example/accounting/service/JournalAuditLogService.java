package com.example.accounting.service;

import com.example.accounting.dto.JournalAuditLogResponse;
import com.example.accounting.entity.JournalAuditAction;
import com.example.accounting.entity.JournalEntry;

import java.util.List;

public interface JournalAuditLogService {

    void record(JournalEntry entry, JournalAuditAction action, String details);

    List<JournalAuditLogResponse> getForEntry(Long journalEntryId);
}
