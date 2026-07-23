package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.InterestSchemeDetailRequest;
import com.example.loanproduct.dto.InterestSchemeDetailResponse;
import com.example.loanproduct.entity.InterestScheme;
import com.example.loanproduct.entity.InterestSchemeDetail;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.InterestSchemeDetailRepository;
import com.example.loanproduct.repository.InterestSchemeRepository;
import com.example.loanproduct.service.InterestSchemeDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterestSchemeDetailServiceImpl implements InterestSchemeDetailService {

    private final InterestSchemeDetailRepository detailRepository;
    private final InterestSchemeRepository interestSchemeRepository;

    @Override
    public InterestSchemeDetailResponse create(UUID interestSchemeId, InterestSchemeDetailRequest request) {
        InterestScheme scheme = findSchemeOrThrow(interestSchemeId);
        InterestSchemeDetail detail = InterestSchemeDetail.builder()
                .interestScheme(scheme)
                .minTerm(request.getMinTerm())
                .maxTerm(request.getMaxTerm())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .interestRate(request.getInterestRate())
                .build();
        return toResponse(detailRepository.save(detail));
    }

    @Override
    public List<InterestSchemeDetailResponse> getByScheme(UUID interestSchemeId) {
        findSchemeOrThrow(interestSchemeId);
        return detailRepository.findByInterestSchemeId(interestSchemeId).stream().map(this::toResponse).toList();
    }

    @Override
    public InterestSchemeDetailResponse update(UUID interestSchemeId, UUID detailId, InterestSchemeDetailRequest request) {
        InterestSchemeDetail detail = findOrThrow(interestSchemeId, detailId);
        detail.setMinTerm(request.getMinTerm());
        detail.setMaxTerm(request.getMaxTerm());
        detail.setMinAmount(request.getMinAmount());
        detail.setMaxAmount(request.getMaxAmount());
        detail.setInterestRate(request.getInterestRate());
        return toResponse(detailRepository.save(detail));
    }

    @Override
    public void delete(UUID interestSchemeId, UUID detailId) {
        detailRepository.delete(findOrThrow(interestSchemeId, detailId));
    }

    private InterestSchemeDetail findOrThrow(UUID interestSchemeId, UUID detailId) {
        return detailRepository.findByIdAndInterestSchemeId(detailId, interestSchemeId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest scheme detail", detailId));
    }

    private InterestScheme findSchemeOrThrow(UUID id) {
        return interestSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest scheme", id));
    }

    private InterestSchemeDetailResponse toResponse(InterestSchemeDetail detail) {
        return InterestSchemeDetailResponse.builder()
                .id(detail.getId())
                .interestSchemeId(detail.getInterestScheme().getId())
                .minTerm(detail.getMinTerm())
                .maxTerm(detail.getMaxTerm())
                .minAmount(detail.getMinAmount())
                .maxAmount(detail.getMaxAmount())
                .interestRate(detail.getInterestRate())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }
}
