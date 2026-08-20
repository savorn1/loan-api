package com.example.loan.dto;

// Mirrors loan-product-service's entity.RuleOperator — see LoanProductRuleResponse.
public enum RuleOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    BETWEEN,
    IN
}
