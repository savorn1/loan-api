package com.example.payment.dto;

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
public class LargeTransactionRow {

    private Long loanId;
    private BigDecimal amount;
    private LocalDate paidAt;
    private Integer installmentNumber;
}
