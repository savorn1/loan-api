package com.example.loanproduct.service;

import com.example.loanproduct.dto.LoanProductDocumentRequest;
import com.example.loanproduct.dto.LoanProductDocumentResponse;

import java.util.List;
import java.util.UUID;

public interface LoanProductDocumentService {

    LoanProductDocumentResponse create(UUID loanProductId, LoanProductDocumentRequest request);

    List<LoanProductDocumentResponse> getByLoanProduct(UUID loanProductId);

    List<LoanProductDocumentResponse> getAll();

    LoanProductDocumentResponse update(UUID loanProductId, Long documentId, LoanProductDocumentRequest request);

    void delete(UUID loanProductId, Long documentId);
}
