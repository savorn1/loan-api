package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.TermTemplateRequest;
import com.example.loanproduct.dto.TermTemplateResponse;
import com.example.loanproduct.entity.TermTemplate;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.TermTemplateRepository;
import com.example.loanproduct.service.TermTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TermTemplateServiceImpl implements TermTemplateService {

    private final TermTemplateRepository termTemplateRepository;

    @Override
    public TermTemplateResponse create(TermTemplateRequest request) {
        TermTemplate template = TermTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .termValue(request.getTermValue())
                .status(request.getStatus())
                .build();
        return toResponse(termTemplateRepository.save(template));
    }

    @Override
    public TermTemplateResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<TermTemplateResponse> getAll() {
        return termTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TermTemplateResponse update(UUID id, TermTemplateRequest request) {
        TermTemplate template = findOrThrow(id);
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setTermValue(request.getTermValue());
        template.setStatus(request.getStatus());
        return toResponse(termTemplateRepository.save(template));
    }

    @Override
    public void delete(UUID id) {
        termTemplateRepository.delete(findOrThrow(id));
    }

    private TermTemplate findOrThrow(UUID id) {
        return termTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term template", id));
    }

    private TermTemplateResponse toResponse(TermTemplate template) {
        return TermTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .termValue(template.getTermValue())
                .status(template.getStatus())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
