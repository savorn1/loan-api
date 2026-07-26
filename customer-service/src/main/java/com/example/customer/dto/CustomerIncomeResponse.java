package com.example.customer.dto;

import com.example.customer.entity.IncomeFrequency;
import com.example.customer.entity.IncomeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerIncomeResponse {

    private Long id;
    private Long customerId;
    private IncomeType incomeType;
    private BigDecimal amount;
    private String currency;
    private IncomeFrequency frequency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
