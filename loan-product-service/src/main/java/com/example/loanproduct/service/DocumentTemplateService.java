package com.example.loanproduct.service;

import com.example.loanproduct.dto.DocumentTemplateRequest;
import com.example.loanproduct.dto.DocumentTemplateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentTemplateService {

    DocumentTemplateResponse create(DocumentTemplateRequest request);

    DocumentTemplateResponse getById(UUID id);

    List<DocumentTemplateResponse> getAll();

    DocumentTemplateResponse update(UUID id, DocumentTemplateRequest request);

    void delete(UUID id);

    DocumentTemplateResponse uploadSampleFile(UUID id, MultipartFile file);

    DocumentTemplateResponse deleteSampleFile(UUID id);
}
