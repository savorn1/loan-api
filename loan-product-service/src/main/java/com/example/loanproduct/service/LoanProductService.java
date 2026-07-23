package com.example.loanproduct.service;

import com.example.loanproduct.dto.LoanProductRequest;
import com.example.loanproduct.dto.LoanProductResponse;

import java.util.List;
import java.util.UUID;

public interface LoanProductService {

    LoanProductResponse create(LoanProductRequest request);

    LoanProductResponse getById(UUID id);

    List<LoanProductResponse> getAll();

    LoanProductResponse update(UUID id, LoanProductRequest request);

    void delete(UUID id);

    LoanProductResponse publish(UUID id);

    LoanProductResponse newVersion(UUID id);

    LoanProductResponse retire(UUID id);
}
