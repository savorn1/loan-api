package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// One line of the individual postings that make up the current period's Loans Receivable
// balance — the starting point for investigating a reconciliation mismatch, since the
// summary check (matched/variance) can't say which posting(s) are responsible.
// referenceType/referenceId point back at loan-service's own record (almost always a
// LoanTransaction id — see LoanServiceImpl.recordTransaction) for further lookup there.
@Data
@Builder
public class ReconciliationPostingResponse {

    private String entryNo;
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private String referenceType;
    private String referenceId;
    private EntrySide entrySide;
    private BigDecimal amount;
    private String description;
}
