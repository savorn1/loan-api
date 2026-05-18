package com.example.loan.service;

import com.example.loan.dto.LoanRequest;
import com.example.loan.dto.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanResponse create(LoanRequest request);

    LoanResponse getById(Long id);

    List<LoanResponse> getAll();

    List<LoanResponse> getByCustomer(Long customerId);

    LoanResponse approve(Long id);

    LoanResponse reject(Long id);

    void delete(Long id);
}
