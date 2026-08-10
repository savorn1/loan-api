package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetLineResponse {

    private Long id;
    private Long financialPeriodId;
    private String financialPeriodName;
    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private BigDecimal budgetAmount;
}
