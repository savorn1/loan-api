package com.example.loanproduct.dto;

import com.example.loanproduct.entity.InterestRateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductInterestSchemeResponse {

    private UUID id;
    private UUID loanProductId;
    private UUID interestSchemeId;
    private String interestSchemeCode;
    private String interestSchemeName;
    private Integer priority;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isDefault;
    private InterestRateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
