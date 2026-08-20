package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EligibilityCheckResponse {

    private String ruleCode;
    private String ruleName;
    private RuleField field;
    private RuleOperator operator;
    // Formatted from the rule's value/value2, e.g. "650" or "21 - 60".
    private String expectedValue;
    // Null when result is NOT_EVALUABLE.
    private String actualValue;
    private EligibilityResult result;
    // Set only when result is NOT_EVALUABLE — why no actual value could be resolved.
    private String reason;
}
