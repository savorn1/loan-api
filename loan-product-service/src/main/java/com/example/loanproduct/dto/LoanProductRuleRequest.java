package com.example.loanproduct.dto;

import com.example.loanproduct.entity.RuleTemplateStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LoanProductRuleRequest {

    @NotNull
    private UUID ruleTemplateId;

    @NotNull
    private RuleTemplateStatus status;
}
