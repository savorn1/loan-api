package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BorrowerConcentrationRow {

    private Long customerId;
    private long loanCount;
    private BigDecimal outstandingBalance;
    private BigDecimal percentOfPortfolio;
}
