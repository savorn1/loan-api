package com.example.accounting.dto;

import com.example.accounting.entity.FinancialPeriodStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FinancialPeriodResponse {

    private Long id;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private FinancialPeriodStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
