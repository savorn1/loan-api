package com.example.loanproduct.service;

import com.example.loanproduct.dto.LoanProductFeeSchemeRequest;
import com.example.loanproduct.dto.LoanProductFeeSchemeResponse;

import java.util.List;
import java.util.UUID;

public interface LoanProductFeeSchemeService {

    LoanProductFeeSchemeResponse create(UUID loanProductId, LoanProductFeeSchemeRequest request);

    List<LoanProductFeeSchemeResponse> getByLoanProduct(UUID loanProductId);

    List<LoanProductFeeSchemeResponse> getAll();

    LoanProductFeeSchemeResponse update(UUID loanProductId, UUID mappingId, LoanProductFeeSchemeRequest request);

    void delete(UUID loanProductId, UUID mappingId);
}
