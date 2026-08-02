package com.example.accounting.dto;

import java.math.BigDecimal;

import com.example.accounting.entity.EntrySide;
public record AccountSideTotal(Long glAccountId, EntrySide entrySide, BigDecimal total) {
}
