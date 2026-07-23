package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.LoanProductDocumentRequest;
import com.example.loanproduct.dto.LoanProductDocumentResponse;
import com.example.loanproduct.entity.DocumentTemplate;
import com.example.loanproduct.entity.LoanProduct;
import com.example.loanproduct.entity.LoanProductDocument;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.DocumentTemplateRepository;
import com.example.loanproduct.repository.LoanProductDocumentRepository;
import com.example.loanproduct.repository.LoanProductRepository;
import com.example.loanproduct.service.LoanProductDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanProductDocumentServiceImpl implements LoanProductDocumentService {

    private final LoanProductDocumentRepository documentRepository;
    private final LoanProductRepository loanProductRepository;
    private final DocumentTemplateRepository documentTemplateRepository;

    @Override
    public LoanProductDocumentResponse create(UUID loanProductId, LoanProductDocumentRequest request) {
        LoanProduct loanProduct = findLoanProductOrThrow(loanProductId);
        DocumentTemplate template = findTemplateOrThrow(request.getDocumentTemplateId());
        LoanProductDocument document = LoanProductDocument.builder()
                .loanProduct(loanProduct)
                .documentTemplate(template)
                .required(request.getRequired())
                .status(request.getStatus())
                .build();
        return toResponse(documentRepository.save(document));
    }

    @Override
    public List<LoanProductDocumentResponse> getByLoanProduct(UUID loanProductId) {
        findLoanProductOrThrow(loanProductId);
        return documentRepository.findByLoanProductId(loanProductId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<LoanProductDocumentResponse> getAll() {
        return documentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public LoanProductDocumentResponse update(UUID loanProductId, Long documentId, LoanProductDocumentRequest request) {
        DocumentTemplate template = findTemplateOrThrow(request.getDocumentTemplateId());
        LoanProductDocument document = findOrThrow(loanProductId, documentId);
        document.setDocumentTemplate(template);
        document.setRequired(request.getRequired());
        document.setStatus(request.getStatus());
        return toResponse(documentRepository.save(document));
    }

    @Override
    public void delete(UUID loanProductId, Long documentId) {
        documentRepository.delete(findOrThrow(loanProductId, documentId));
    }

    private LoanProductDocument findOrThrow(UUID loanProductId, Long documentId) {
        return documentRepository.findByIdAndLoanProductId(documentId, loanProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
    }

    private LoanProduct findLoanProductOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private DocumentTemplate findTemplateOrThrow(UUID id) {
        return documentTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document template", id));
    }

    private LoanProductDocumentResponse toResponse(LoanProductDocument document) {
        DocumentTemplate template = document.getDocumentTemplate();
        return LoanProductDocumentResponse.builder()
                .id(document.getId())
                .loanProductId(document.getLoanProduct().getId())
                .documentTemplateId(template.getId())
                .documentTemplateCode(template.getCode())
                .documentTemplateName(template.getName())
                .documentTemplateDescription(template.getDescription())
                .required(document.getRequired())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
