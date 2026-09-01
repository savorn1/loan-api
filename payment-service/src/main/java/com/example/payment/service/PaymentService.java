package com.example.payment.service;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.GenerateScheduleRequest;
import com.example.payment.dto.MarkPaidByInstallmentRequest;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatusHistoryResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse create(PaymentRequest request);

    PaymentResponse getById(Long id);

    PageResponse<PaymentResponse> getAll(int page, int size, String sortBy, String sortOrder);

    List<PaymentResponse> getByLoan(Long loanId);

    PaymentResponse markAsPaid(Long id, String changedBy);

    // Reverse-sync counterpart to markAsPaid, called by loan-service instead of a
    // user — see MarkPaidByInstallmentRequest for why this can't reuse markAsPaid.
    // Silently no-ops if there's no matching row or it's already PAID.
    void markPaidByInstallment(MarkPaidByInstallmentRequest request);

    List<PaymentStatusHistoryResponse> getStatusHistory(Long id);

    List<PaymentResponse> createSchedule(GenerateScheduleRequest request);

    void delete(Long id);
}
