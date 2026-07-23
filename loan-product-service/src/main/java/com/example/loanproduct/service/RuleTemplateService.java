package com.example.loanproduct.service;

import com.example.loanproduct.dto.RuleTemplateRequest;
import com.example.loanproduct.dto.RuleTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface RuleTemplateService {

    RuleTemplateResponse create(RuleTemplateRequest request);

    RuleTemplateResponse getById(UUID id);

    List<RuleTemplateResponse> getAll();

    RuleTemplateResponse update(UUID id, RuleTemplateRequest request);

    void delete(UUID id);
}
