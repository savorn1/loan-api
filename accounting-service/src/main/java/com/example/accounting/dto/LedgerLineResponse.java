package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LedgerLineResponse {

    private String entryNo;
    private LocalDate transactionDate;
    private String description;
    private EntrySide entrySide;
    private BigDecimal amount;

    // Signed cumulative balance as of this line, in the account's normal-balance direction.
    private BigDecimal runningBalance;
}
