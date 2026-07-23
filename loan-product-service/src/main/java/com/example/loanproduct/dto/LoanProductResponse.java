package com.example.loanproduct.dto;

import com.example.loanproduct.entity.LoanProductStatus;
import com.example.loanproduct.entity.LoanType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private LoanType loanType;
    private String currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTerm;
    private Integer maxTerm;
    private LoanProductStatus status;
    private Integer version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
