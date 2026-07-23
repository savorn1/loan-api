package com.example.accounting.service.impl;

import com.example.accounting.dto.JournalTemplateLineRequest;
import com.example.accounting.dto.JournalTemplateLineResponse;
import com.example.accounting.dto.JournalTemplateRequest;
import com.example.accounting.dto.JournalTemplateResponse;
import com.example.accounting.entity.JournalTemplate;
import com.example.accounting.entity.JournalTemplateLine;
import com.example.accounting.exception.AppException;
import com.example.accounting.exception.ResourceNotFoundException;
import com.example.accounting.repository.JournalTemplateRepository;
import com.example.accounting.service.JournalTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalTemplateServiceImpl implements JournalTemplateService {

    private final JournalTemplateRepository journalTemplateRepository;

    @Override
    @Transactional
    public JournalTemplateResponse create(JournalTemplateRequest request) {
        if (journalTemplateRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Journal template code already exists: " + request.getCode());
        }
        JournalTemplate template = JournalTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
        applyLines(template, request.getLines());
        return toResponse(journalTemplateRepository.save(template));
    }

    @Override
    public JournalTemplateResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<JournalTemplateResponse> getAll() {
        return journalTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public JournalTemplateResponse update(Long id, JournalTemplateRequest request) {
        JournalTemplate template = findOrThrow(id);
        if (!template.getCode().equals(request.getCode()) && journalTemplateRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Journal template code already exists: " + request.getCode());
        }
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setTransactionType(request.getTransactionType());
        template.setDescription(request.getDescription());
        template.setStatus(request.getStatus());
        template.getLines().clear();
        applyLines(template, request.getLines());
        return toResponse(journalTemplateRepository.save(template));
    }

    @Override
    public void delete(Long id) {
        journalTemplateRepository.delete(findOrThrow(id));
    }

    private void applyLines(JournalTemplate template, List<JournalTemplateLineRequest> lineRequests) {
        lineRequests.stream()
                .map(lr -> JournalTemplateLine.builder()
                        .journalTemplate(template)
                        .lineNo(lr.getLineNo())
                        .accountRole(lr.getAccountRole())
                        .entrySide(lr.getEntrySide())
                        .description(lr.getDescription())
                        .build())
                .forEach(template.getLines()::add);
    }

    private JournalTemplate findOrThrow(Long id) {
        return journalTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Journal template", id));
    }

    private JournalTemplateResponse toResponse(JournalTemplate template) {
        List<JournalTemplateLineResponse> lines = template.getLines().stream()
                .map(l -> JournalTemplateLineResponse.builder()
                        .id(l.getId())
                        .lineNo(l.getLineNo())
                        .accountRole(l.getAccountRole())
                        .entrySide(l.getEntrySide())
                        .description(l.getDescription())
                        .build())
                .toList();
        return JournalTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .transactionType(template.getTransactionType())
                .description(template.getDescription())
                .status(template.getStatus())
                .lines(lines)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
