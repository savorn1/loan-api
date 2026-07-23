package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.LoanProductRuleRequest;
import com.example.loanproduct.dto.LoanProductRuleResponse;
import com.example.loanproduct.entity.LoanProduct;
import com.example.loanproduct.entity.LoanProductRule;
import com.example.loanproduct.entity.RuleTemplate;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.LoanProductRepository;
import com.example.loanproduct.repository.LoanProductRuleRepository;
import com.example.loanproduct.repository.RuleTemplateRepository;
import com.example.loanproduct.service.LoanProductRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanProductRuleServiceImpl implements LoanProductRuleService {

    private final LoanProductRuleRepository ruleRepository;
    private final LoanProductRepository loanProductRepository;
    private final RuleTemplateRepository ruleTemplateRepository;

    @Override
    public LoanProductRuleResponse create(UUID loanProductId, LoanProductRuleRequest request) {
        LoanProduct loanProduct = findLoanProductOrThrow(loanProductId);
        RuleTemplate template = findTemplateOrThrow(request.getRuleTemplateId());
        LoanProductRule rule = LoanProductRule.builder()
                .loanProduct(loanProduct)
                .ruleTemplate(template)
                .status(request.getStatus())
                .build();
        return toResponse(ruleRepository.save(rule));
    }

    @Override
    public List<LoanProductRuleResponse> getByLoanProduct(UUID loanProductId) {
        findLoanProductOrThrow(loanProductId);
        return ruleRepository.findByLoanProductId(loanProductId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<LoanProductRuleResponse> getAll() {
        return ruleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public LoanProductRuleResponse update(UUID loanProductId, Long ruleId, LoanProductRuleRequest request) {
        RuleTemplate template = findTemplateOrThrow(request.getRuleTemplateId());
        LoanProductRule rule = findOrThrow(loanProductId, ruleId);
        rule.setRuleTemplate(template);
        rule.setStatus(request.getStatus());
        return toResponse(ruleRepository.save(rule));
    }

    @Override
    public void delete(UUID loanProductId, Long ruleId) {
        ruleRepository.delete(findOrThrow(loanProductId, ruleId));
    }

    private LoanProductRule findOrThrow(UUID loanProductId, Long ruleId) {
        return ruleRepository.findByIdAndLoanProductId(ruleId, loanProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Rule", ruleId));
    }

    private LoanProduct findLoanProductOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private RuleTemplate findTemplateOrThrow(UUID id) {
        return ruleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule template", id));
    }

    private LoanProductRuleResponse toResponse(LoanProductRule rule) {
        RuleTemplate template = rule.getRuleTemplate();
        return LoanProductRuleResponse.builder()
                .id(rule.getId())
                .loanProductId(rule.getLoanProduct().getId())
                .ruleTemplateId(template.getId())
                .ruleTemplateCode(template.getCode())
                .ruleTemplateName(template.getName())
                .field(template.getField())
                .operator(template.getOperator())
                .value(template.getValue())
                .value2(template.getValue2())
                .description(template.getDescription())
                .status(rule.getStatus())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
