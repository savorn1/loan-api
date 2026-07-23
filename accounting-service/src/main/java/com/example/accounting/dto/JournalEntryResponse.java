package com.example.accounting.dto;

import com.example.accounting.entity.JournalEntryStatus;
import com.example.accounting.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JournalEntryResponse {

    private Long id;
    private String entryNo;
    private TransactionType transactionType;
    private LocalDate transactionDate;
    private Long financialPeriodId;
    private String financialPeriodName;
    private String referenceType;
    private String referenceId;
    private String currency;
    private String description;
    private JournalEntryStatus status;
    private LocalDateTime postedAt;
    private String postedBy;
    private List<JournalEntryLineResponse> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
