package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Lines span multiple GL accounts with different normal-balance directions, so unlike
// GeneralLedgerResponse/DateRangeLedgerResponse there's no single running balance —
// only debit/credit totals across the branch's posted activity.
@Data
@Builder
public class BranchLedgerResponse {

    private Long branchId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private List<BranchLedgerLineResponse> lines;
}
