package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanRefinanceResponse {

    private Long id;
    private Long loanId;
    private Long newLoanId;
    private String reason;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
