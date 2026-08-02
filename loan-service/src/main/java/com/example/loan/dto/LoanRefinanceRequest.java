package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanRefinanceRequest {

    @NotNull
    private Long newLoanId;

    @NotBlank
    private String reason;

    @NotNull
    private LocalDate effectiveDate;
}
