package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanRestructureResponse {

    private Long id;
    private Long loanId;
    private Integer newTermMonths;
    private BigDecimal newInterestRate;
    private String reason;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
