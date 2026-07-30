package com.example.payment.service.impl;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentGatewayRequest;
import com.example.payment.dto.PaymentGatewayResponse;
import com.example.payment.entity.PaymentGateway;
import com.example.payment.exception.AppException;
import com.example.payment.exception.ResourceNotFoundException;
import com.example.payment.repository.PaymentGatewayRepository;
import com.example.payment.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final PaymentGatewayRepository paymentGatewayRepository;

    @Override
    public PaymentGatewayResponse create(PaymentGatewayRequest request) {
        if (paymentGatewayRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Payment gateway code already exists: " + request.getCode());
        }
        PaymentGateway gateway = PaymentGateway.builder()
                .code(request.getCode())
                .name(request.getName())
                .provider(request.getProvider())
                .apiUrl(request.getApiUrl())
                .status(request.getStatus())
                .build();
        return toResponse(paymentGatewayRepository.save(gateway));
    }

    @Override
    public PaymentGatewayResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<PaymentGatewayResponse> getAll(int page, int size, String sortBy, String sortOrder, String search) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        Page<PaymentGateway> result = StringUtils.hasText(search)
                ? paymentGatewayRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(search, search, pageable)
                : paymentGatewayRepository.findAll(pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    @Override
    public PaymentGatewayResponse update(Long id, PaymentGatewayRequest request) {
        PaymentGateway gateway = findOrThrow(id);
        if (!gateway.getCode().equals(request.getCode())
                && paymentGatewayRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Payment gateway code already exists: " + request.getCode());
        }
        gateway.setCode(request.getCode());
        gateway.setName(request.getName());
        gateway.setProvider(request.getProvider());
        gateway.setApiUrl(request.getApiUrl());
        gateway.setStatus(request.getStatus());
        return toResponse(paymentGatewayRepository.save(gateway));
    }

    @Override
    public void delete(Long id) {
        paymentGatewayRepository.delete(findOrThrow(id));
    }

    private PaymentGateway findOrThrow(Long id) {
        return paymentGatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment gateway", id));
    }

    private PaymentGatewayResponse toResponse(PaymentGateway gateway) {
        return PaymentGatewayResponse.builder()
                .id(gateway.getId())
                .code(gateway.getCode())
                .name(gateway.getName())
                .provider(gateway.getProvider())
                .apiUrl(gateway.getApiUrl())
                .status(gateway.getStatus())
                .createdAt(gateway.getCreatedAt())
                .updatedAt(gateway.getUpdatedAt())
                .build();
    }
}
