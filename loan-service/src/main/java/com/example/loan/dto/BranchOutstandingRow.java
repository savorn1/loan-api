package com.example.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchOutstandingRow {

    private Long branchId;
    private long loanCount;
    private BigDecimal outstandingBalance;
}
