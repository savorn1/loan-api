package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.RuleTemplateRequest;
import com.example.loanproduct.dto.RuleTemplateResponse;
import com.example.loanproduct.entity.RuleOperator;
import com.example.loanproduct.entity.RuleTemplate;
import com.example.loanproduct.exception.AppException;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.RuleTemplateRepository;
import com.example.loanproduct.service.RuleTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleTemplateServiceImpl implements RuleTemplateService {

    private final RuleTemplateRepository ruleTemplateRepository;

    @Override
    public RuleTemplateResponse create(RuleTemplateRequest request) {
        validateBetweenValue2(request);
        RuleTemplate template = RuleTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .field(request.getField())
                .operator(request.getOperator())
                .value(request.getValue())
                .value2(request.getOperator() == RuleOperator.BETWEEN ? request.getValue2() : null)
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
        return toResponse(ruleTemplateRepository.save(template));
    }

    @Override
    public RuleTemplateResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<RuleTemplateResponse> getAll() {
        return ruleTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public RuleTemplateResponse update(UUID id, RuleTemplateRequest request) {
        validateBetweenValue2(request);
        RuleTemplate template = findOrThrow(id);
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setField(request.getField());
        template.setOperator(request.getOperator());
        template.setValue(request.getValue());
        template.setValue2(request.getOperator() == RuleOperator.BETWEEN ? request.getValue2() : null);
        template.setDescription(request.getDescription());
        template.setStatus(request.getStatus());
        return toResponse(ruleTemplateRepository.save(template));
    }

    @Override
    public void delete(UUID id) {
        ruleTemplateRepository.delete(findOrThrow(id));
    }

    private void validateBetweenValue2(RuleTemplateRequest request) {
        if (request.getOperator() == RuleOperator.BETWEEN && !StringUtils.hasText(request.getValue2())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "value2 (upper bound) is required when operator is BETWEEN");
        }
    }

    private RuleTemplate findOrThrow(UUID id) {
        return ruleTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule template", id));
    }

    private RuleTemplateResponse toResponse(RuleTemplate template) {
        return RuleTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .field(template.getField())
                .operator(template.getOperator())
                .value(template.getValue())
                .value2(template.getValue2())
                .description(template.getDescription())
                .status(template.getStatus())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
