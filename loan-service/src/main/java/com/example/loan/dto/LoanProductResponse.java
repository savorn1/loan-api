package com.example.loan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

// Local mirror of loan-product-service's dto.LoanProductResponse — only the fields this
// service needs: display name (Group/ApplicationResponse.loanProductName) and range
// validation (ApplicationServiceImpl.resolveAndValidateProduct). status is a plain String
// (compared against "PUBLISHED"), not a shared enum, so it can't fail deserialization if
// loan-product-service ever adds/renames a status value.
@Data
public class LoanProductResponse {

    private UUID id;
    private String name;
    private String status;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTerm;
    private Integer maxTerm;
}
