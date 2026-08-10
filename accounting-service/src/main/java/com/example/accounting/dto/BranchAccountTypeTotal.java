package com.example.accounting.dto;

import java.math.BigDecimal;

import com.example.accounting.entity.AccountType;
import com.example.accounting.entity.EntrySide;

// One (branch, account type, entry side) group, pivoted by ReportServiceImpl into
// IncomeByBranchRow — same shape/purpose as AccountSideTotal, just with branchId and
// accountType added since this aggregates across accounts, not one account at a time.
public record BranchAccountTypeTotal(Long branchId, AccountType accountType, EntrySide entrySide, BigDecimal total) {
}
