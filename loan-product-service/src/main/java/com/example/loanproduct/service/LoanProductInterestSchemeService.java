package com.example.loanproduct.service;

import com.example.loanproduct.dto.LoanProductInterestSchemeRequest;
import com.example.loanproduct.dto.LoanProductInterestSchemeResponse;

import java.util.List;
import java.util.UUID;

public interface LoanProductInterestSchemeService {

    LoanProductInterestSchemeResponse create(UUID loanProductId, LoanProductInterestSchemeRequest request);

    List<LoanProductInterestSchemeResponse> getByLoanProduct(UUID loanProductId);

    List<LoanProductInterestSchemeResponse> getAll();

    LoanProductInterestSchemeResponse update(UUID loanProductId, UUID mappingId, LoanProductInterestSchemeRequest request);

    LoanProductInterestSchemeResponse setDefault(UUID loanProductId, UUID mappingId);

    void delete(UUID loanProductId, UUID mappingId);
}
