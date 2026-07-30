package com.example.payment.service;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentChannelRequest;
import com.example.payment.dto.PaymentChannelResponse;

public interface PaymentChannelService {

    PaymentChannelResponse create(PaymentChannelRequest request);

    PaymentChannelResponse getById(Long id);

    PageResponse<PaymentChannelResponse> getAll(int page, int size, String sortBy, String sortOrder, String search);

    PaymentChannelResponse update(Long id, PaymentChannelRequest request);

    void delete(Long id);
}
