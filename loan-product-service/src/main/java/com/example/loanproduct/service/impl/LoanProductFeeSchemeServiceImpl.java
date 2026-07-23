package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.LoanProductFeeSchemeRequest;
import com.example.loanproduct.dto.LoanProductFeeSchemeResponse;
import com.example.loanproduct.entity.FeeScheme;
import com.example.loanproduct.entity.LoanProduct;
import com.example.loanproduct.entity.LoanProductFeeScheme;
import com.example.loanproduct.exception.AppException;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.FeeSchemeRepository;
import com.example.loanproduct.repository.LoanProductFeeSchemeRepository;
import com.example.loanproduct.repository.LoanProductRepository;
import com.example.loanproduct.service.LoanProductFeeSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanProductFeeSchemeServiceImpl implements LoanProductFeeSchemeService {

    private final LoanProductFeeSchemeRepository mappingRepository;
    private final LoanProductRepository loanProductRepository;
    private final FeeSchemeRepository feeSchemeRepository;

    @Override
    public LoanProductFeeSchemeResponse create(UUID loanProductId, LoanProductFeeSchemeRequest request) {
        LoanProduct loanProduct = findLoanProductOrThrow(loanProductId);
        FeeScheme scheme = findSchemeOrThrow(request.getFeeSchemeId());
        validateEffectiveDates(request);
        LoanProductFeeScheme mapping = LoanProductFeeScheme.builder()
                .loanProduct(loanProduct)
                .feeScheme(scheme)
                .isMandatory(request.getIsMandatory())
                .priority(request.getPriority())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .status(request.getStatus())
                .build();
        return toResponse(mappingRepository.save(mapping));
    }

    @Override
    public List<LoanProductFeeSchemeResponse> getByLoanProduct(UUID loanProductId) {
        findLoanProductOrThrow(loanProductId);
        return mappingRepository.findByLoanProductId(loanProductId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<LoanProductFeeSchemeResponse> getAll() {
        return mappingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public LoanProductFeeSchemeResponse update(UUID loanProductId, UUID mappingId, LoanProductFeeSchemeRequest request) {
        FeeScheme scheme = findSchemeOrThrow(request.getFeeSchemeId());
        validateEffectiveDates(request);
        LoanProductFeeScheme mapping = findOrThrow(loanProductId, mappingId);
        mapping.setFeeScheme(scheme);
        mapping.setIsMandatory(request.getIsMandatory());
        mapping.setPriority(request.getPriority());
        mapping.setEffectiveFrom(request.getEffectiveFrom());
        mapping.setEffectiveTo(request.getEffectiveTo());
        mapping.setStatus(request.getStatus());
        return toResponse(mappingRepository.save(mapping));
    }

    @Override
    public void delete(UUID loanProductId, UUID mappingId) {
        mappingRepository.delete(findOrThrow(loanProductId, mappingId));
    }

    private void validateEffectiveDates(LoanProductFeeSchemeRequest request) {
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "effectiveTo must be on or after effectiveFrom");
        }
    }

    private LoanProductFeeScheme findOrThrow(UUID loanProductId, UUID mappingId) {
        return mappingRepository.findByIdAndLoanProductId(mappingId, loanProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product fee scheme", mappingId));
    }

    private LoanProduct findLoanProductOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private FeeScheme findSchemeOrThrow(UUID id) {
        return feeSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee scheme", id));
    }

    private LoanProductFeeSchemeResponse toResponse(LoanProductFeeScheme mapping) {
        return LoanProductFeeSchemeResponse.builder()
                .id(mapping.getId())
                .loanProductId(mapping.getLoanProduct().getId())
                .feeSchemeId(mapping.getFeeScheme().getId())
                .feeSchemeCode(mapping.getFeeScheme().getCode())
                .feeSchemeName(mapping.getFeeScheme().getName())
                .isMandatory(mapping.getIsMandatory())
                .priority(mapping.getPriority())
                .effectiveFrom(mapping.getEffectiveFrom())
                .effectiveTo(mapping.getEffectiveTo())
                .status(mapping.getStatus())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }
}
