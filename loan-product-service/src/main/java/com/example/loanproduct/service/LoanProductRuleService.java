package com.example.loanproduct.service;

import com.example.loanproduct.dto.LoanProductRuleRequest;
import com.example.loanproduct.dto.LoanProductRuleResponse;

import java.util.List;
import java.util.UUID;

public interface LoanProductRuleService {

    LoanProductRuleResponse create(UUID loanProductId, LoanProductRuleRequest request);

    List<LoanProductRuleResponse> getByLoanProduct(UUID loanProductId);

    List<LoanProductRuleResponse> getAll();

    LoanProductRuleResponse update(UUID loanProductId, Long ruleId, LoanProductRuleRequest request);

    void delete(UUID loanProductId, Long ruleId);
}
