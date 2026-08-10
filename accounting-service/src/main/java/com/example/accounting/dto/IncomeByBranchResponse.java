package com.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class IncomeByBranchResponse {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<IncomeByBranchRow> rows;
}
