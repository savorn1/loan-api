package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Statement of financial position as of a date — cumulative over every posted entry
 * up to and including {@code asOfDate}, not scoped to one financial period.
 *
 * <p>{@code currentEarnings} is the part worth understanding: closing a financial
 * period in this system only flips its status, it posts no closing entries, so INCOME
 * and EXPENSE accounts are never rolled into Retained Earnings. Their cumulative net
 * is therefore surfaced here as its own equity line — without it assets would never
 * equal liabilities + equity. {@code balanced} reports whether they do; a false value
 * means unbalanced postings exist and the books need investigating.
 */
@Data
@Builder
public class BalanceSheetResponse {

    private LocalDate asOfDate;
    private List<FinancialStatementLine> assets;
    private List<FinancialStatementLine> liabilities;
    private List<FinancialStatementLine> equity;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    /** Sum of the posted EQUITY accounts only — excludes {@link #currentEarnings}. */
    private BigDecimal totalEquity;
    /** Cumulative income minus expenses to date, i.e. P&amp;L not yet closed to equity. */
    private BigDecimal currentEarnings;
    /** totalLiabilities + totalEquity + currentEarnings. */
    private BigDecimal totalLiabilitiesAndEquity;
    private boolean balanced;
}
