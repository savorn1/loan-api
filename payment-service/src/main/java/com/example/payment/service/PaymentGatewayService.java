package com.example.payment.service;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentGatewayRequest;
import com.example.payment.dto.PaymentGatewayResponse;

public interface PaymentGatewayService {

    PaymentGatewayResponse create(PaymentGatewayRequest request);

    PaymentGatewayResponse getById(Long id);

    PageResponse<PaymentGatewayResponse> getAll(int page, int size, String sortBy, String sortOrder, String search);

    PaymentGatewayResponse update(Long id, PaymentGatewayRequest request);

    void delete(Long id);
}
