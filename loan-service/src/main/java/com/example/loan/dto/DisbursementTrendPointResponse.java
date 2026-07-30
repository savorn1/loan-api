package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementTrendPointResponse {

    private String month;
    private long loanCount;
    private BigDecimal totalDisbursed;
}
