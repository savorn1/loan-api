package com.example.payment.service.impl;

import com.example.payment.common.PageResponse;
import com.example.payment.dto.PaymentChannelRequest;
import com.example.payment.dto.PaymentChannelResponse;
import com.example.payment.entity.PaymentChannel;
import com.example.payment.exception.AppException;
import com.example.payment.exception.ResourceNotFoundException;
import com.example.payment.repository.PaymentChannelRepository;
import com.example.payment.service.PaymentChannelService;
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
public class PaymentChannelServiceImpl implements PaymentChannelService {

    private final PaymentChannelRepository paymentChannelRepository;

    @Override
    public PaymentChannelResponse create(PaymentChannelRequest request) {
        if (paymentChannelRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Payment channel code already exists: " + request.getCode());
        }
        PaymentChannel channel = PaymentChannel.builder()
                .code(request.getCode())
                .name(request.getName())
                .channelType(request.getChannelType())
                .status(request.getStatus())
                .build();
        return toResponse(paymentChannelRepository.save(channel));
    }

    @Override
    public PaymentChannelResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public PageResponse<PaymentChannelResponse> getAll(int page, int size, String sortBy, String sortOrder, String search) {
        Sort sort = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, sort);
        Page<PaymentChannel> result = StringUtils.hasText(search)
                ? paymentChannelRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(search, search, pageable)
                : paymentChannelRepository.findAll(pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    @Override
    public PaymentChannelResponse update(Long id, PaymentChannelRequest request) {
        PaymentChannel channel = findOrThrow(id);
        if (!channel.getCode().equals(request.getCode())
                && paymentChannelRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Payment channel code already exists: " + request.getCode());
        }
        channel.setCode(request.getCode());
        channel.setName(request.getName());
        channel.setChannelType(request.getChannelType());
        channel.setStatus(request.getStatus());
        return toResponse(paymentChannelRepository.save(channel));
    }

    @Override
    public void delete(Long id) {
        paymentChannelRepository.delete(findOrThrow(id));
    }

    private PaymentChannel findOrThrow(Long id) {
        return paymentChannelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment channel", id));
    }

    private PaymentChannelResponse toResponse(PaymentChannel channel) {
        return PaymentChannelResponse.builder()
                .id(channel.getId())
                .code(channel.getCode())
                .name(channel.getName())
                .channelType(channel.getChannelType())
                .status(channel.getStatus())
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .build();
    }
}
