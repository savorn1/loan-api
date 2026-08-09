package com.example.loan.dto;

import com.example.loan.entity.DisbursementMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanWriteoffRecoveryResponse {

    private Long id;
    private Long writeoffId;
    private Long loanId;
    private BigDecimal amount;
    private LocalDate recoveryDate;
    private DisbursementMethod method;
    private String reference;
    private LocalDateTime createdAt;
}
