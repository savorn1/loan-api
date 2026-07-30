package com.example.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal principal;
    private BigDecimal outstandingBalance;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String status;
}
