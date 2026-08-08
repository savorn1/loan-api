package com.example.accounting.dto;

import com.example.accounting.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Fired by upstream services (loan-service on disbursement, payment-service on payment
// allocation) instead of them building JournalEntryLineRequest[] themselves — the caller
// only knows the business event and its single amount; resolving that into GL account
// lines is this service's job (via the transactionType's JournalTemplate + AccountingScheme).
// currency is not a field here: every scheme currently configured is USD, so it's hardcoded
// in JournalEntryServiceImpl.generate rather than trusted from the caller.
@Data
public class JournalEntryGenerateRequest {

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private LocalDate transactionDate;

    // Not @NotNull: JournalEntry.branchId is itself a nullable column (branch is optional
    // metadata, not part of the double-entry balance) and loan-service's own Loan.branchId
    // is nullable too — most loans in this dataset don't have one set. Rejecting a null here
    // would fail every generate() call for those loans for no accounting-correctness reason.
    private Long branchId;

    @NotNull
    @Size(max = 50)
    private String referenceType;

    @NotNull
    @Size(max = 100)
    private String referenceId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String description;
}
