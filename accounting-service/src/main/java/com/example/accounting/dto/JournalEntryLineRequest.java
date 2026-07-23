package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalEntryLineRequest {

    @NotNull
    private Integer lineNo;

    @NotNull
    private Long glAccountId;

    @NotNull
    private EntrySide entrySide;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private String description;
}
