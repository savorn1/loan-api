package com.example.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;

// Subset of loan-service's own PortfolioSummaryResponse — reconciliation only needs the
// receivable total, not active loan count or original principal.
@Data
public class LoanPortfolioSummaryResponse {

    private BigDecimal totalOutstandingBalance;
}
