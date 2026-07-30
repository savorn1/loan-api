package com.example.payment.service.impl;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentMethodRequest;
import com.example.payment.dto.PaymentMethodResponse;
import com.example.payment.entity.PaymentMethod;
import com.example.payment.exception.AppException;
import com.example.payment.exception.ResourceNotFoundException;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.service.PaymentMethodService;
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
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public PaymentMethodResponse create(PaymentMethodRequest request) {
        if (paymentMethodRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Payment method code already exists: " + request.getCode());
        }
        PaymentMethod method = PaymentMethod.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .status(request.getStatus())
                .build();
        return toResponse(paymentMethodRepository.save(method));
    }

    @Override
    public PaymentMethodResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<PaymentMethodResponse> getAll(int page, int size, String sortBy, String sortOrder, String search) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        Page<PaymentMethod> result = StringUtils.hasText(search)
                ? paymentMethodRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(search, search, pageable)
                : paymentMethodRepository.findAll(pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    @Override
    public PaymentMethodResponse update(Long id, PaymentMethodRequest request) {
        PaymentMethod method = findOrThrow(id);
        if (!method.getCode().equals(request.getCode())
                && paymentMethodRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Payment method code already exists: " + request.getCode());
        }
        method.setCode(request.getCode());
        method.setName(request.getName());
        method.setType(request.getType());
        method.setStatus(request.getStatus());
        return toResponse(paymentMethodRepository.save(method));
    }

    @Override
    public void delete(Long id) {
        paymentMethodRepository.delete(findOrThrow(id));
    }

    private PaymentMethod findOrThrow(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method", id));
    }

    private PaymentMethodResponse toResponse(PaymentMethod method) {
        return PaymentMethodResponse.builder()
                .id(method.getId())
                .code(method.getCode())
                .name(method.getName())
                .type(method.getType())
                .status(method.getStatus())
                .createdAt(method.getCreatedAt())
                .updatedAt(method.getUpdatedAt())
                .build();
    }
}
