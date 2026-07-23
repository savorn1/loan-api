package com.example.loan.dto;

import com.example.loan.entity.WriteoffStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanWriteoffResponse {

    private Long id;
    private Long loanId;
    private BigDecimal amount;
    private String reason;
    private LocalDate writeoffDate;
    private WriteoffStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
