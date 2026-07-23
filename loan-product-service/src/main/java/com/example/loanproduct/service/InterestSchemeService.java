package com.example.loanproduct.service;

import com.example.loanproduct.dto.InterestSchemeRequest;
import com.example.loanproduct.dto.InterestSchemeResponse;

import java.util.List;
import java.util.UUID;

public interface InterestSchemeService {

    InterestSchemeResponse create(InterestSchemeRequest request);

    InterestSchemeResponse getById(UUID id);

    List<InterestSchemeResponse> getAll();

    InterestSchemeResponse update(UUID id, InterestSchemeRequest request);

    void delete(UUID id);
}
