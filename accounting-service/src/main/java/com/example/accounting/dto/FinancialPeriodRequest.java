package com.example.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FinancialPeriodRequest {

    @NotBlank
    @Size(max = 20)
    private String periodName;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
