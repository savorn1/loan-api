package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.DocumentTemplateRequest;
import com.example.loanproduct.dto.DocumentTemplateResponse;
import com.example.loanproduct.entity.DocumentTemplate;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.DocumentTemplateRepository;
import com.example.loanproduct.service.DocumentTemplateService;
import com.example.storage.FileStorageService;
import com.example.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

    private final DocumentTemplateRepository documentTemplateRepository;
    private final FileStorageService fileStorageService;

    @Override
    public DocumentTemplateResponse create(DocumentTemplateRequest request) {
        DocumentTemplate template = DocumentTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
        return toResponse(documentTemplateRepository.save(template));
    }

    @Override
    public DocumentTemplateResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<DocumentTemplateResponse> getAll() {
        return documentTemplateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DocumentTemplateResponse update(UUID id, DocumentTemplateRequest request) {
        DocumentTemplate template = findOrThrow(id);
        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setStatus(request.getStatus());
        return toResponse(documentTemplateRepository.save(template));
    }

    @Override
    public void delete(UUID id) {
        documentTemplateRepository.delete(findOrThrow(id));
    }

    @Override
    public DocumentTemplateResponse uploadSampleFile(UUID id, MultipartFile file) {
        DocumentTemplate template = findOrThrow(id);
        StoredFile stored = fileStorageService.upload(file, "document-templates/" + id);
        template.setSampleFileName(file.getOriginalFilename());
        template.setSampleFileUrl(stored.url());
        return toResponse(documentTemplateRepository.save(template));
    }

    @Override
    public DocumentTemplateResponse deleteSampleFile(UUID id) {
        DocumentTemplate template = findOrThrow(id);
        template.setSampleFileName(null);
        template.setSampleFileUrl(null);
        return toResponse(documentTemplateRepository.save(template));
    }

    private DocumentTemplate findOrThrow(UUID id) {
        return documentTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document template", id));
    }

    private DocumentTemplateResponse toResponse(DocumentTemplate template) {
        return DocumentTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .description(template.getDescription())
                .status(template.getStatus())
                .sampleFileName(template.getSampleFileName())
                .sampleFileUrl(template.getSampleFileUrl())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
