package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Same shape as GeneralLedgerResponse but scoped to a free-form date range instead of a
// financial period — backs both the "Ledger by Date Range" report (dateFrom/dateTo required)
// and "Account Transaction History" (both null = full history, opening balance is zero).
@Data
@Builder
public class DateRangeLedgerResponse {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal openingBalance;
    private BigDecimal periodDebitTotal;
    private BigDecimal periodCreditTotal;
    private BigDecimal closingBalance;
    private List<LedgerLineResponse> lines;
}
