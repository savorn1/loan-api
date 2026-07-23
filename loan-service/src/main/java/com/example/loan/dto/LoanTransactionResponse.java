package com.example.loan.dto;

import com.example.loan.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanTransactionResponse {

    private Long id;
    private Long loanId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String referenceType;
    private Long referenceId;
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
