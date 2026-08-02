package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BranchLedgerLineResponse {

    private String entryNo;
    private LocalDate transactionDate;
    private String glAccountNo;
    private String glAccountName;
    private String description;
    private EntrySide entrySide;
    private BigDecimal amount;
}
