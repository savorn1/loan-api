package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class GeneralLedgerResponse {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private Long financialPeriodId;
    private String financialPeriodName;
    private BigDecimal openingBalance;
    private BigDecimal periodDebitTotal;
    private BigDecimal periodCreditTotal;
    private BigDecimal closingBalance;
    private List<LedgerLineResponse> lines;
}
