package com.example.loan.dto;

import lombok.Data;

import java.util.UUID;

// Local mirror of loan-product-service's dto.LoanProductRuleResponse — only what
// EligibilityServiceImpl needs to evaluate a product's assigned eligibility rules.
// status is a plain String (compared against "ACTIVE"), same pattern as
// LoanProductResponse, so it can't fail deserialization if loan-product-service
// ever adds/renames a status value.
@Data
public class LoanProductRuleResponse {

    private UUID loanProductId;
    private String ruleTemplateCode;
    private String ruleTemplateName;
    private RuleField field;
    private RuleOperator operator;
    private String value;
    private String value2;
    private String status;
}
