package com.example.accounting.service.impl;

import com.example.accounting.dto.JournalAuditLogResponse;
import com.example.accounting.entity.JournalAuditAction;
import com.example.accounting.entity.JournalAuditLog;
import com.example.accounting.entity.JournalEntry;
import com.example.accounting.repository.JournalAuditLogRepository;
import com.example.accounting.service.JournalAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalAuditLogServiceImpl implements JournalAuditLogService {

    private final JournalAuditLogRepository journalAuditLogRepository;

    @Override
    public void record(JournalEntry entry, JournalAuditAction action, String details) {
        journalAuditLogRepository.save(JournalAuditLog.builder()
                .journalEntry(entry)
                .action(action)
                .performedBy(currentUsername())
                .performedAt(LocalDateTime.now())
                .details(details)
                .build());
    }

    @Override
    public List<JournalAuditLogResponse> getForEntry(Long journalEntryId) {
        return journalAuditLogRepository.findByJournalEntryIdOrderByPerformedAtAsc(journalEntryId).stream()
                .map(log -> JournalAuditLogResponse.builder()
                        .id(log.getId())
                        .journalEntryId(log.getJournalEntry().getId())
                        .action(log.getAction())
                        .performedBy(log.getPerformedBy())
                        .performedAt(log.getPerformedAt())
                        .details(log.getDetails())
                        .build())
                .toList();
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
