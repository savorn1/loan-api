package com.example.loanproduct.dto;

import com.example.loanproduct.entity.FeeSchemeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanProductFeeSchemeRequest {

    @NotNull
    private UUID feeSchemeId;

    @NotNull
    private Boolean isMandatory;

    @NotNull
    private Integer priority;

    @NotNull
    private LocalDate effectiveFrom;

    // Optional — no end date means the assignment is open-ended (still in effect).
    private LocalDate effectiveTo;

    @NotNull
    private FeeSchemeStatus status;
}
