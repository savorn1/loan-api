package com.example.loan.dto;

import com.example.loan.entity.DisbursementMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanDisbursementRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDate disbursedDate;

    @NotNull
    private DisbursementMethod method;

    private String reference;
}
