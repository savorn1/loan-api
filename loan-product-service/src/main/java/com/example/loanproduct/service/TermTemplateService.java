package com.example.loanproduct.service;

import com.example.loanproduct.dto.TermTemplateRequest;
import com.example.loanproduct.dto.TermTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface TermTemplateService {

    TermTemplateResponse create(TermTemplateRequest request);

    TermTemplateResponse getById(UUID id);

    List<TermTemplateResponse> getAll();

    TermTemplateResponse update(UUID id, TermTemplateRequest request);

    void delete(UUID id);
}
