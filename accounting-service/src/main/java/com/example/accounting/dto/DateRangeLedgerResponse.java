package com.example.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DateRangeLedgerResponse {

    private Long glAccountId;
    private String accountNo;
    private String accountName;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal openingBalance;
    private BigDecimal periodDebitTotal;
    private BigDecimal periodCreditTotal;
    private BigDecimal closingBalance;
    private List<LedgerLineResponse> lines;
}
