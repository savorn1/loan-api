package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ConcentrationRiskResponse {

    private BigDecimal totalOutstandingBalance;
    private List<BranchConcentrationRow> byBranch;
    private List<BorrowerConcentrationRow> topBorrowers;
}
