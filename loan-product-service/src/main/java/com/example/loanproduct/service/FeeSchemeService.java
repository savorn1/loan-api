package com.example.loanproduct.service;

import com.example.loanproduct.dto.FeeSchemeRequest;
import com.example.loanproduct.dto.FeeSchemeResponse;

import java.util.List;
import java.util.UUID;

public interface FeeSchemeService {

    FeeSchemeResponse create(FeeSchemeRequest request);

    FeeSchemeResponse getById(UUID id);

    List<FeeSchemeResponse> getAll();

    FeeSchemeResponse update(UUID id, FeeSchemeRequest request);

    void delete(UUID id);
}
