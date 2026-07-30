package com.example.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

// Minimal projection of loan-service's own PortfolioSummaryResponse — only
// what the PAR summary needs for its denominator.
@Data
public class PortfolioSummaryResponse {

    private long activeLoanCount;
    private BigDecimal totalOutstandingBalance;
}
