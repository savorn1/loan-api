package com.example.loanproduct.dto;

import com.example.loanproduct.entity.RuleField;
import com.example.loanproduct.entity.RuleOperator;
import com.example.loanproduct.entity.RuleTemplateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductRuleResponse {

    private Long id;
    private UUID loanProductId;
    private UUID ruleTemplateId;
    private String ruleTemplateCode;
    private String ruleTemplateName;
    private RuleField field;
    private RuleOperator operator;
    private String value;
    private String value2;
    private String description;
    private RuleTemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
