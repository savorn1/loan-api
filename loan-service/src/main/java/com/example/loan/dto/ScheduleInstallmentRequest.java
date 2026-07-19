package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInstallmentRequest {

    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal amount;
}
