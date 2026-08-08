package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Mirrors accounting-service's JournalEntryGenerateRequest (see accounting-service's
// JournalEntryController /api/journal-entries/generate). transactionType is a raw String
// here rather than loan-service's own TransactionType enum — the two enums don't line up
// 1:1 (e.g. loan-service's PENALTY_PAYMENT vs accounting-service's PENALTY_CHARGE), so
// LoanServiceImpl.toAccountingTransactionType maps explicitly instead of relying on a
// shared enum. currency isn't sent — accounting-service hardcodes USD on its side.
@Data
@Builder
public class JournalEntryGenerateRequest {

    private String transactionType;
    private LocalDate transactionDate;
    private Long branchId;
    private String referenceType;
    private String referenceId;
    private BigDecimal amount;
    private String description;
}
