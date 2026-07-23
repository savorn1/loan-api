package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.FeeSchemeRequest;
import com.example.loanproduct.dto.FeeSchemeResponse;
import com.example.loanproduct.entity.FeeScheme;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.FeeSchemeRepository;
import com.example.loanproduct.service.FeeSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeeSchemeServiceImpl implements FeeSchemeService {

    private final FeeSchemeRepository feeSchemeRepository;

    @Override
    public FeeSchemeResponse create(FeeSchemeRequest request) {
        FeeScheme scheme = FeeScheme.builder()
                .code(request.getCode())
                .name(request.getName())
                .status(request.getStatus())
                .build();
        return toResponse(feeSchemeRepository.save(scheme));
    }

    @Override
    public FeeSchemeResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<FeeSchemeResponse> getAll() {
        return feeSchemeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public FeeSchemeResponse update(UUID id, FeeSchemeRequest request) {
        FeeScheme scheme = findOrThrow(id);
        scheme.setCode(request.getCode());
        scheme.setName(request.getName());
        scheme.setStatus(request.getStatus());
        return toResponse(feeSchemeRepository.save(scheme));
    }

    @Override
    public void delete(UUID id) {
        feeSchemeRepository.delete(findOrThrow(id));
    }

    private FeeScheme findOrThrow(UUID id) {
        return feeSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee scheme", id));
    }

    private FeeSchemeResponse toResponse(FeeScheme scheme) {
        return FeeSchemeResponse.builder()
                .id(scheme.getId())
                .code(scheme.getCode())
                .name(scheme.getName())
                .status(scheme.getStatus())
                .createdAt(scheme.getCreatedAt())
                .updatedAt(scheme.getUpdatedAt())
                .build();
    }
}
