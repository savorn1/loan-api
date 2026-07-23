package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TrialBalanceRowResponse {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;

    // totalDebit - totalCredit for DEBIT-normal accounts, totalCredit - totalDebit for CREDIT-normal.
    private BigDecimal balance;
}
