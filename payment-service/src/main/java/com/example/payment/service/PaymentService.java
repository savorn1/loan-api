package com.example.payment.service;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.GenerateScheduleRequest;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse create(PaymentRequest request);

    PaymentResponse getById(Long id);

    PageResponse<PaymentResponse> getAll(int page, int size, String sortBy, String sortOrder);

    List<PaymentResponse> getByLoan(Long loanId);

    PaymentResponse markAsPaid(Long id);

    List<PaymentResponse> createSchedule(GenerateScheduleRequest request);

    void delete(Long id);
}
