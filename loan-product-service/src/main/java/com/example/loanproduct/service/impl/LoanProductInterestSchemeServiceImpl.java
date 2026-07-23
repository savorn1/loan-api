package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.LoanProductInterestSchemeRequest;
import com.example.loanproduct.dto.LoanProductInterestSchemeResponse;
import com.example.loanproduct.entity.InterestScheme;
import com.example.loanproduct.entity.LoanProduct;
import com.example.loanproduct.entity.LoanProductInterestScheme;
import com.example.loanproduct.exception.AppException;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.InterestSchemeRepository;
import com.example.loanproduct.repository.LoanProductInterestSchemeRepository;
import com.example.loanproduct.repository.LoanProductRepository;
import com.example.loanproduct.service.LoanProductInterestSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanProductInterestSchemeServiceImpl implements LoanProductInterestSchemeService {

    private final LoanProductInterestSchemeRepository mappingRepository;
    private final LoanProductRepository loanProductRepository;
    private final InterestSchemeRepository interestSchemeRepository;

    @Override
    @Transactional
    public LoanProductInterestSchemeResponse create(UUID loanProductId, LoanProductInterestSchemeRequest request) {
        LoanProduct loanProduct = findLoanProductOrThrow(loanProductId);
        InterestScheme scheme = findSchemeOrThrow(request.getInterestSchemeId());
        validateEffectiveDates(request);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearExistingDefault(loanProductId);
        }
        LoanProductInterestScheme mapping = LoanProductInterestScheme.builder()
                .loanProduct(loanProduct)
                .interestScheme(scheme)
                .priority(request.getPriority())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .isDefault(request.getIsDefault())
                .status(request.getStatus())
                .build();
        return toResponse(mappingRepository.save(mapping));
    }

    @Override
    public List<LoanProductInterestSchemeResponse> getByLoanProduct(UUID loanProductId) {
        findLoanProductOrThrow(loanProductId);
        return mappingRepository.findByLoanProductId(loanProductId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<LoanProductInterestSchemeResponse> getAll() {
        return mappingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public LoanProductInterestSchemeResponse update(UUID loanProductId, UUID mappingId, LoanProductInterestSchemeRequest request) {
        InterestScheme scheme = findSchemeOrThrow(request.getInterestSchemeId());
        validateEffectiveDates(request);
        LoanProductInterestScheme mapping = findOrThrow(loanProductId, mappingId);
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(mapping.getIsDefault())) {
            clearExistingDefault(loanProductId);
        }
        mapping.setInterestScheme(scheme);
        mapping.setPriority(request.getPriority());
        mapping.setEffectiveFrom(request.getEffectiveFrom());
        mapping.setEffectiveTo(request.getEffectiveTo());
        mapping.setIsDefault(request.getIsDefault());
        mapping.setStatus(request.getStatus());
        return toResponse(mappingRepository.save(mapping));
    }

    @Override
    @Transactional
    public LoanProductInterestSchemeResponse setDefault(UUID loanProductId, UUID mappingId) {
        LoanProductInterestScheme mapping = findOrThrow(loanProductId, mappingId);
        clearExistingDefault(loanProductId);
        mapping.setIsDefault(true);
        return toResponse(mappingRepository.save(mapping));
    }

    @Override
    public void delete(UUID loanProductId, UUID mappingId) {
        mappingRepository.delete(findOrThrow(loanProductId, mappingId));
    }

    private void validateEffectiveDates(LoanProductInterestSchemeRequest request) {
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "effectiveTo must be on or after effectiveFrom");
        }
    }

    // At most one default interest scheme per product — unset whichever mapping currently holds it.
    private void clearExistingDefault(UUID loanProductId) {
        List<LoanProductInterestScheme> current = mappingRepository.findByLoanProductIdAndIsDefaultTrue(loanProductId);
        current.forEach(m -> m.setIsDefault(false));
        mappingRepository.saveAll(current);
    }

    private LoanProductInterestScheme findOrThrow(UUID loanProductId, UUID mappingId) {
        return mappingRepository.findByIdAndLoanProductId(mappingId, loanProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product interest scheme", mappingId));
    }

    private LoanProduct findLoanProductOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan product", id));
    }

    private InterestScheme findSchemeOrThrow(UUID id) {
        return interestSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest scheme", id));
    }

    private LoanProductInterestSchemeResponse toResponse(LoanProductInterestScheme mapping) {
        return LoanProductInterestSchemeResponse.builder()
                .id(mapping.getId())
                .loanProductId(mapping.getLoanProduct().getId())
                .interestSchemeId(mapping.getInterestScheme().getId())
                .interestSchemeCode(mapping.getInterestScheme().getCode())
                .interestSchemeName(mapping.getInterestScheme().getName())
                .priority(mapping.getPriority())
                .effectiveFrom(mapping.getEffectiveFrom())
                .effectiveTo(mapping.getEffectiveTo())
                .isDefault(mapping.getIsDefault())
                .status(mapping.getStatus())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }
}
