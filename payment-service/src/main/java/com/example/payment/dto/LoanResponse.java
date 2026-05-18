package com.example.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanResponse {

    private Long id;
    private Long customerId;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String status;
}
