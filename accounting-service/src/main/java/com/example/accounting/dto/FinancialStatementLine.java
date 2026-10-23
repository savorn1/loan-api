package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * One GL account's contribution to a financial statement section. {@code amount} is
 * already normalized for the account's normal balance (the same convention
 * {@link TrialBalanceRowResponse#getBalance()} uses), so a positive number always
 * means "more of what this account is for" — more asset, more income, more expense —
 * regardless of which side it was posted on.
 */
@Data
@Builder
public class FinancialStatementLine {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private BigDecimal amount;
}
