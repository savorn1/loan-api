package com.example.loanproduct.service;

import com.example.loanproduct.dto.LoanProductTermRequest;
import com.example.loanproduct.dto.LoanProductTermResponse;

import java.util.List;
import java.util.UUID;

public interface LoanProductTermService {

    LoanProductTermResponse create(UUID loanProductId, LoanProductTermRequest request);

    List<LoanProductTermResponse> getByLoanProduct(UUID loanProductId);

    List<LoanProductTermResponse> getAll();

    LoanProductTermResponse update(UUID loanProductId, Long termId, LoanProductTermRequest request);

    LoanProductTermResponse setDefault(UUID loanProductId, Long termId);

    void delete(UUID loanProductId, Long termId);
}
