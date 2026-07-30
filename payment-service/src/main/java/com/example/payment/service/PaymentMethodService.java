package com.example.payment.service;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentMethodRequest;
import com.example.payment.dto.PaymentMethodResponse;

public interface PaymentMethodService {

    PaymentMethodResponse create(PaymentMethodRequest request);

    PaymentMethodResponse getById(Long id);

    PageResponse<PaymentMethodResponse> getAll(int page, int size, String sortBy, String sortOrder, String search);

    PaymentMethodResponse update(Long id, PaymentMethodRequest request);

    void delete(Long id);
}
