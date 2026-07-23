package com.example.accounting.dto;

import com.example.accounting.entity.EntrySide;

import java.math.BigDecimal;

// JPQL constructor-expression projection used by JournalEntryLineRepository to aggregate
// posted line amounts per (account, side) for the trial balance report.
public record AccountSideTotal(Long glAccountId, EntrySide entrySide, BigDecimal total) {
}
