package com.example.accounting.dto;

import com.example.accounting.entity.JournalAuditAction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JournalAuditLogResponse {

    private Long id;
    private Long journalEntryId;
    private JournalAuditAction action;
    private String performedBy;
    private LocalDateTime performedAt;
    private String details;
}
