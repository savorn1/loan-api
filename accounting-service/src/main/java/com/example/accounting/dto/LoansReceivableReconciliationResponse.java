package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoansReceivableReconciliationResponse {

    private String glAccountNo;
    private BigDecimal glBalance;
    private BigDecimal loanServiceOutstandingTotal;
    private BigDecimal variance;
    private boolean matched;
}
