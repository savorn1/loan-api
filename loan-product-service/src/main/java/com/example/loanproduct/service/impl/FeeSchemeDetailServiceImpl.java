package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.FeeSchemeDetailRequest;
import com.example.loanproduct.dto.FeeSchemeDetailResponse;
import com.example.loanproduct.entity.FeeScheme;
import com.example.loanproduct.entity.FeeSchemeDetail;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.FeeSchemeDetailRepository;
import com.example.loanproduct.repository.FeeSchemeRepository;
import com.example.loanproduct.service.FeeSchemeDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeeSchemeDetailServiceImpl implements FeeSchemeDetailService {

    private final FeeSchemeDetailRepository detailRepository;
    private final FeeSchemeRepository feeSchemeRepository;

    @Override
    public FeeSchemeDetailResponse create(UUID feeSchemeId, FeeSchemeDetailRequest request) {
        FeeScheme scheme = findSchemeOrThrow(feeSchemeId);
        FeeSchemeDetail detail = FeeSchemeDetail.builder()
                .feeScheme(scheme)
                .type(request.getType())
                .calculationMethod(request.getCalculationMethod())
                .amount(request.getAmount())
                .chargeTiming(request.getChargeTiming())
                .build();
        return toResponse(detailRepository.save(detail));
    }

    @Override
    public List<FeeSchemeDetailResponse> getByScheme(UUID feeSchemeId) {
        findSchemeOrThrow(feeSchemeId);
        return detailRepository.findByFeeSchemeId(feeSchemeId).stream().map(this::toResponse).toList();
    }

    @Override
    public FeeSchemeDetailResponse update(UUID feeSchemeId, UUID detailId, FeeSchemeDetailRequest request) {
        FeeSchemeDetail detail = findOrThrow(feeSchemeId, detailId);
        detail.setType(request.getType());
        detail.setCalculationMethod(request.getCalculationMethod());
        detail.setAmount(request.getAmount());
        detail.setChargeTiming(request.getChargeTiming());
        return toResponse(detailRepository.save(detail));
    }

    @Override
    public void delete(UUID feeSchemeId, UUID detailId) {
        detailRepository.delete(findOrThrow(feeSchemeId, detailId));
    }

    private FeeSchemeDetail findOrThrow(UUID feeSchemeId, UUID detailId) {
        return detailRepository.findByIdAndFeeSchemeId(detailId, feeSchemeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee scheme detail", detailId));
    }

    private FeeScheme findSchemeOrThrow(UUID id) {
        return feeSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee scheme", id));
    }

    private FeeSchemeDetailResponse toResponse(FeeSchemeDetail detail) {
        return FeeSchemeDetailResponse.builder()
                .id(detail.getId())
                .feeSchemeId(detail.getFeeScheme().getId())
                .type(detail.getType())
                .calculationMethod(detail.getCalculationMethod())
                .amount(detail.getAmount())
                .chargeTiming(detail.getChargeTiming())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }
}
