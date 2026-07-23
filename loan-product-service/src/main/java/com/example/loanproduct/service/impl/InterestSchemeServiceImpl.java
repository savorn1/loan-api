package com.example.loanproduct.service.impl;

import com.example.loanproduct.dto.InterestSchemeRequest;
import com.example.loanproduct.dto.InterestSchemeResponse;
import com.example.loanproduct.entity.InterestScheme;
import com.example.loanproduct.exception.ResourceNotFoundException;
import com.example.loanproduct.repository.InterestSchemeRepository;
import com.example.loanproduct.service.InterestSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterestSchemeServiceImpl implements InterestSchemeService {

    private final InterestSchemeRepository interestSchemeRepository;

    @Override
    public InterestSchemeResponse create(InterestSchemeRequest request) {
        InterestScheme scheme = InterestScheme.builder()
                .code(request.getCode())
                .name(request.getName())
                .interestType(request.getInterestType())
                .calculationMethod(request.getCalculationMethod())
                .status(request.getStatus())
                .build();
        return toResponse(interestSchemeRepository.save(scheme));
    }

    @Override
    public InterestSchemeResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<InterestSchemeResponse> getAll() {
        return interestSchemeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public InterestSchemeResponse update(UUID id, InterestSchemeRequest request) {
        InterestScheme scheme = findOrThrow(id);
        scheme.setCode(request.getCode());
        scheme.setName(request.getName());
        scheme.setInterestType(request.getInterestType());
        scheme.setCalculationMethod(request.getCalculationMethod());
        scheme.setStatus(request.getStatus());
        return toResponse(interestSchemeRepository.save(scheme));
    }

    @Override
    public void delete(UUID id) {
        interestSchemeRepository.delete(findOrThrow(id));
    }

    private InterestScheme findOrThrow(UUID id) {
        return interestSchemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interest scheme", id));
    }

    private InterestSchemeResponse toResponse(InterestScheme scheme) {
        return InterestSchemeResponse.builder()
                .id(scheme.getId())
                .code(scheme.getCode())
                .name(scheme.getName())
                .interestType(scheme.getInterestType())
                .calculationMethod(scheme.getCalculationMethod())
                .status(scheme.getStatus())
                .createdAt(scheme.getCreatedAt())
                .updatedAt(scheme.getUpdatedAt())
                .build();
    }
}
