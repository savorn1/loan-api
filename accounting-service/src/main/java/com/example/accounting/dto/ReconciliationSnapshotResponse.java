package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReconciliationSnapshotResponse {

    private Long id;
    private LocalDateTime checkedAt;
    private String glAccountNo;
    private BigDecimal glBalance;
    private BigDecimal loanServiceOutstandingTotal;
    private BigDecimal variance;
    private boolean matched;
}
