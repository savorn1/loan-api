package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.LoanProductTermRequest;
import com.example.loanproduct.dto.LoanProductTermResponse;
import com.example.loanproduct.entity.LoanProduct;
import com.example.loanproduct.entity.LoanProductTerm;
import com.example.loanproduct.entity.TermTemplate;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.LoanProductRepository;
import com.example.loanproduct.repository.LoanProductTermRepository;
import com.example.loanproduct.repository.TermTemplateRepository;
import com.example.loanproduct.service.LoanProductTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanProductTermServiceImpl implements LoanProductTermService {

    private final LoanProductTermRepository termRepository;
    private final LoanProductRepository loanProductRepository;
    private final TermTemplateRepository termTemplateRepository;

    @Override
    @Transactional
    public LoanProductTermResponse create(UUID loanProductId, LoanProductTermRequest request) {
        LoanProduct loanProduct = findLoanProductOrThrow(loanProductId);
        TermTemplate template = findTemplateOrThrow(request.getTermTemplateId());
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearExistingDefault(loanProductId);
        }
        LoanProductTerm term = LoanProductTerm.builder()
                .loanProduct(loanProduct)
                .termTemplate(template)
                .isDefault(request.getIsDefault())
                .status(request.getStatus())
                .build();
        return toResponse(termRepository.save(term));
    }

    @Override
    public List<LoanProductTermResponse> getByLoanProduct(UUID loanProductId) {
        findLoanProductOrThrow(loanProductId);
        return termRepository.findByLoanProductId(loanProductId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<LoanProductTermResponse> getAll() {
        return termRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public LoanProductTermResponse update(UUID loanProductId, Long termId, LoanProductTermRequest request) {
        TermTemplate template = findTemplateOrThrow(request.getTermTemplateId());
        LoanProductTerm term = findTermOrThrow(loanProductId, termId);
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(term.getIsDefault())) {
            clearExistingDefault(loanProductId);
        }
        term.setTermTemplate(template);
        term.setIsDefault(request.getIsDefault());
        term.setStatus(request.getStatus());
        return toResponse(termRepository.save(term));
    }

    @Override
    @Transactional
    public LoanProductTermResponse setDefault(UUID loanProductId, Long termId) {
        LoanProductTerm term = findTermOrThrow(loanProductId, termId);
        clearExistingDefault(loanProductId);
        term.setIsDefault(true);
        return toResponse(termRepository.save(term));
    }

    @Override
    public void delete(UUID loanProductId, Long termId) {
        termRepository.delete(findTermOrThrow(loanProductId, termId));
    }

    // At most one default term per product — unset whichever term currently holds it.
    private void clearExistingDefault(UUID loanProductId) {
        List<LoanProductTerm> current = termRepository.findByLoanProductIdAndIsDefaultTrue(loanProductId);
        current.forEach(t -> t.setIsDefault(false));
        termRepository.saveAll(current);
    }

    private LoanProductTerm findTermOrThrow(UUID loanProductId, Long termId) {
        return termRepository.findByIdAndLoanProductId(termId, loanProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Term", termId));
    }

    private LoanProduct findLoanProductOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private TermTemplate findTemplateOrThrow(UUID id) {
        return termTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term template", id));
    }

    private LoanProductTermResponse toResponse(LoanProductTerm term) {
        return LoanProductTermResponse.builder()
                .id(term.getId())
                .loanProductId(term.getLoanProduct().getId())
                .termTemplateId(term.getTermTemplate().getId())
                .termTemplateCode(term.getTermTemplate().getCode())
                .termTemplateName(term.getTermTemplate().getName())
                .termValue(term.getTermTemplate().getTermValue())
                .isDefault(term.getIsDefault())
                .status(term.getStatus())
                .createdAt(term.getCreatedAt())
                .updatedAt(term.getUpdatedAt())
                .build();
    }
}
