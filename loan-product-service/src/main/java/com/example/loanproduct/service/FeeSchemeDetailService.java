package com.example.loanproduct.service;

import com.example.loanproduct.dto.FeeSchemeDetailRequest;
import com.example.loanproduct.dto.FeeSchemeDetailResponse;

import java.util.List;
import java.util.UUID;

public interface FeeSchemeDetailService {

    FeeSchemeDetailResponse create(UUID feeSchemeId, FeeSchemeDetailRequest request);

    List<FeeSchemeDetailResponse> getByScheme(UUID feeSchemeId);

    FeeSchemeDetailResponse update(UUID feeSchemeId, UUID detailId, FeeSchemeDetailRequest request);

    void delete(UUID feeSchemeId, UUID detailId);
}
