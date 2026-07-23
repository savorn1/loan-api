package com.example.loanproduct.dto;

import com.example.loanproduct.entity.LoanProductStatus;
import com.example.loanproduct.entity.LoanType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanProductRequest {

    @NotBlank
    @Size(max = 20)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    private LoanType loanType;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minAmount;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal maxAmount;

    @NotNull
    @Min(1)
    private Integer minTerm;

    @NotNull
    @Min(1)
    private Integer maxTerm;

    @NotNull
    private LoanProductStatus status;

    @NotNull
    private LocalDate effectiveFrom;

    // Optional — no end date means the product is open-ended (still in effect).
    private LocalDate effectiveTo;
}
