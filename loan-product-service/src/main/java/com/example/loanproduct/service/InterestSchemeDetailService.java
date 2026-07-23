package com.example.loanproduct.service;

import com.example.loanproduct.dto.InterestSchemeDetailRequest;
import com.example.loanproduct.dto.InterestSchemeDetailResponse;

import java.util.List;
import java.util.UUID;

public interface InterestSchemeDetailService {

    InterestSchemeDetailResponse create(UUID interestSchemeId, InterestSchemeDetailRequest request);

    List<InterestSchemeDetailResponse> getByScheme(UUID interestSchemeId);

    InterestSchemeDetailResponse update(UUID interestSchemeId, UUID detailId, InterestSchemeDetailRequest request);

    void delete(UUID interestSchemeId, UUID detailId);
}
