package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPricingRow {

    private BigDecimal interestRate;
    private BigDecimal principal;
    private Integer termMonths;
}
