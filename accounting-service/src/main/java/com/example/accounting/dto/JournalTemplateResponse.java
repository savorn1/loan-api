package com.example.accounting.dto;

import com.example.accounting.entity.JournalTemplateStatus;
import com.example.accounting.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JournalTemplateResponse {

    private Long id;
    private String code;
    private String name;
    private TransactionType transactionType;
    private String description;
    private JournalTemplateStatus status;
    private List<JournalTemplateLineResponse> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
