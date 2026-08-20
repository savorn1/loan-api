package com.example.loan.dto;

import lombok.Data;

import java.math.BigDecimal;

// Local mirror of customer-service's dto.CustomerIncomeResponse — only what
// EligibilityServiceImpl needs to normalize a customer's income sources to a
// monthly figure for a MONTHLY_INCOME rule. frequency is a plain String rather
// than a shared enum, same reasoning as LoanProductResponse.
@Data
public class CustomerIncomeResponse {

    private BigDecimal amount;
    private String frequency;
}
