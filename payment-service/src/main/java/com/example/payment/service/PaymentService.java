package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse create(PaymentRequest request);

    PaymentResponse getById(Long id);

    List<PaymentResponse> getAll();

    List<PaymentResponse> getByLoan(Long loanId);

    PaymentResponse markAsPaid(Long id);

    void delete(Long id);
}
