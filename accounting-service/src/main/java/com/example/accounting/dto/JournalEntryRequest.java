package com.example.accounting.dto;

import com.example.accounting.entity.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JournalEntryRequest {

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private LocalDate transactionDate;

    // Reference back to the upstream domain event (e.g. a loan-service disbursement id) —
    // opaque to this service, used only for traceability.
    @Size(max = 50)
    private String referenceType;

    @Size(max = 100)
    private String referenceId;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    private String description;

    @NotEmpty
    @Valid
    private List<JournalEntryLineRequest> lines;
}
