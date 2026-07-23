package com.example.loanproduct.dto;

import com.example.loanproduct.entity.FeeSchemeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductFeeSchemeResponse {

    private UUID id;
    private UUID loanProductId;
    private UUID feeSchemeId;
    private String feeSchemeCode;
    private String feeSchemeName;
    private Boolean isMandatory;
    private Integer priority;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private FeeSchemeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
