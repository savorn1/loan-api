package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class JournalEntryLineResponse {

    private Long id;
    private Integer lineNo;
    private Long glAccountId;
    private String glAccountNo;
    private EntrySide entrySide;
    private BigDecimal amount;
    private String description;
}
