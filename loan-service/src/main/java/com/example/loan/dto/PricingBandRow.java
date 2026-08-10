package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingBandRow {

    private String band;
    private long loanCount;
    private BigDecimal totalPrincipal;
    private BigDecimal avgInterestRate;
}
