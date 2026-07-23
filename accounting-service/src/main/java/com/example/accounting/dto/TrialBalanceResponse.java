package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TrialBalanceResponse {

    private Long id;
    private Long financialPeriodId;
    private String financialPeriodName;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private List<TrialBalanceRowResponse> lines;
}
