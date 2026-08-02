package com.example.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

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
