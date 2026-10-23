package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Direct-method cash flow: what actually moved through the cash accounts in the
 * window, grouped by the non-cash account on the other side of each entry.
 *
 * <p>Deliberately <em>not</em> the IFRS operating/investing/financing breakdown —
 * that split needs a per-account classification the chart of accounts doesn't carry,
 * and inventing one here would be guesswork dressed up as a statutory statement.
 * This reports what the ledger can actually prove, and reconciles exactly:
 * {@code openingCash + totalInflows - totalOutflows == closingCash}.
 *
 * <p>Which accounts count as cash is configured by {@code accounting.cash-account-numbers}.
 * Transfers between two cash accounts net to zero and are excluded — they move cash
 * around inside the pool without changing it. Where an entry has several non-cash
 * lines on the funding side, the cash amount is split across them in proportion to
 * their amounts, with any rounding remainder going to the largest line so the
 * reconciliation stays exact.
 */
@Data
@Builder
public class CashFlowStatementResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal openingCash;
    private List<CashFlowLine> inflows;
    private List<CashFlowLine> outflows;
    private BigDecimal totalInflows;
    private BigDecimal totalOutflows;
    private BigDecimal netCashMovement;
    private BigDecimal closingCash;
}
