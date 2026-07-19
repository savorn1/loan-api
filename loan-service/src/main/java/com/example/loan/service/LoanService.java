package com.example.loan.service;

import com.example.loan.common.PageResponse;
import com.example.loan.dto.ApplyPaymentRequest;
import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanResponse create(LoanRequest request);

    LoanResponse getById(Long id);

    PageResponse<LoanResponse> getAll(int page, int size, String sortBy, String sortOrder);

    List<LoanResponse> getByCustomer(Long customerId);

    LoanResponse approve(Long id);

    LoanResponse reject(Long id);

    LoanResponse disburse(Long id);

    LoanResponse applyPayment(Long id, ApplyPaymentRequest request);

    void delete(Long id);
}
