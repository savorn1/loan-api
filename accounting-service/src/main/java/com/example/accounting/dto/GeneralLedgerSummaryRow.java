package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// One row per GL account for the "General Ledger" overview report — same balances as
// GeneralLedgerResponse but without the line-level detail, for a period-wide account listing.
@Data
@Builder
public class GeneralLedgerSummaryRow {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private BigDecimal openingBalance;
    private BigDecimal periodDebitTotal;
    private BigDecimal periodCreditTotal;
    private BigDecimal closingBalance;
}
