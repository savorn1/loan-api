package com.example.payment.service.impl;

import com.example.payment.client.CustomerClient;
import com.example.payment.dto.CustomerResponse;
import com.example.payment.dto.PaymentTransactionItemResponse;
import com.example.payment.dto.PaymentTransactionRequest;
import com.example.payment.dto.PaymentTransactionResponse;
import com.example.payment.entity.PaymentChannel;
import com.example.payment.entity.PaymentGateway;
import com.example.payment.entity.PaymentMethod;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.entity.PaymentTransactionItem;
import com.example.payment.entity.TransactionStatus;
import com.example.payment.exception.AppException;
import com.example.payment.exception.ResourceNotFoundException;
import com.example.payment.repository.PaymentChannelRepository;
import com.example.payment.repository.PaymentGatewayRepository;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.repository.PaymentTransactionItemRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import com.example.payment.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionItemRepository paymentTransactionItemRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentChannelRepository paymentChannelRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final CustomerClient customerClient;

    // Only these transitions are allowed — mirrors the two action states the
    // frontend's detail page offers (PENDING -> SUCCESS/FAILED, SUCCESS -> REFUNDED).
    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS = Map.of(
            TransactionStatus.PENDING, EnumSet.of(TransactionStatus.SUCCESS, TransactionStatus.FAILED),
            TransactionStatus.SUCCESS, EnumSet.of(TransactionStatus.REFUNDED)
    );

    @Override
    public PaymentTransactionResponse create(PaymentTransactionRequest request) {
        PaymentMethod method = findMethodOrThrow(request.getPaymentMethodId());
        PaymentChannel channel = findChannelOrThrow(request.getPaymentChannelId());
        PaymentGateway gateway = findGatewayOrThrow(request.getPaymentGatewayId());
        String customerName = resolveCustomerName(request.getCustomerId());

        PaymentTransaction transaction = PaymentTransaction.builder()
                .customerId(request.getCustomerId())
                .paymentMethod(method)
                .paymentChannel(channel)
                .paymentGateway(gateway)
                .businessType(request.getBusinessType())
                .businessReference(request.getBusinessReference())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .referenceNo(request.getReferenceNo())
                .status(TransactionStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        transaction = paymentTransactionRepository.save(transaction);
        transaction.setPaymentNo(generatePaymentNo(transaction.getId()));
        transaction = paymentTransactionRepository.save(transaction);

        PaymentTransactionItem item = paymentTransactionItemRepository.save(PaymentTransactionItem.builder()
                .paymentTransaction(transaction)
                .referenceType(request.getBusinessType())
                .referenceId(parseReferenceId(request.getBusinessReference()))
                .amount(request.getAmount())
                .build());

        return toResponse(transaction, customerName, List.of(toItemResponse(item)));
    }

    @Override
    public PaymentTransactionResponse getById(Long id) {
        PaymentTransaction transaction = findOrThrow(id);
        return toResponse(transaction, resolveCustomerName(transaction.getCustomerId()), itemsFor(id));
    }

    @Override
    public List<PaymentTransactionResponse> getAll() {
        return paymentTransactionRepository.findAll().stream()
                .map(t -> toResponse(t, resolveCustomerName(t.getCustomerId()), itemsFor(t.getId())))
                .toList();
    }

    @Override
    public PaymentTransactionResponse updateStatus(Long id, TransactionStatus status) {
        PaymentTransaction transaction = findOrThrow(id);
        Set<TransactionStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(transaction.getStatus(), Set.of());
        if (!allowed.contains(status)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Cannot transition transaction from " + transaction.getStatus() + " to " + status);
        }
        transaction.setStatus(status);
        if (status == TransactionStatus.SUCCESS || status == TransactionStatus.FAILED) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        transaction = paymentTransactionRepository.save(transaction);
        return toResponse(transaction, resolveCustomerName(transaction.getCustomerId()), itemsFor(id));
    }

    private List<PaymentTransactionItemResponse> itemsFor(Long transactionId) {
        return paymentTransactionItemRepository.findByPaymentTransactionId(transactionId).stream()
                .map(this::toItemResponse)
                .toList();
    }

    private String resolveCustomerName(Long customerId) {
        CustomerResponse customer = customerClient.getById(customerId).getData();
        return customer != null ? customer.getFullName() : null;
    }

    private Long parseReferenceId(String businessReference) {
        if (businessReference == null || businessReference.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(businessReference.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String generatePaymentNo(Long id) {
        return "PMT-" + String.format("%08d", id);
    }

    private PaymentTransaction findOrThrow(Long id) {
        return paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction", id));
    }

    private PaymentMethod findMethodOrThrow(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method", id));
    }

    private PaymentChannel findChannelOrThrow(Long id) {
        return paymentChannelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment channel", id));
    }

    private PaymentGateway findGatewayOrThrow(Long id) {
        return paymentGatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment gateway", id));
    }

    private PaymentTransactionItemResponse toItemResponse(PaymentTransactionItem item) {
        return PaymentTransactionItemResponse.builder()
                .id(item.getId())
                .paymentTransactionId(item.getPaymentTransaction().getId())
                .referenceType(item.getReferenceType())
                .referenceId(item.getReferenceId())
                .amount(item.getAmount())
                .build();
    }

    private PaymentTransactionResponse toResponse(
            PaymentTransaction t, String customerName, List<PaymentTransactionItemResponse> items) {
        return PaymentTransactionResponse.builder()
                .id(t.getId())
                .paymentNo(t.getPaymentNo())
                .referenceNo(t.getReferenceNo())
                .customerId(t.getCustomerId())
                .customerName(customerName)
                .paymentMethodId(t.getPaymentMethod().getId())
                .paymentMethodName(t.getPaymentMethod().getName())
                .paymentChannelId(t.getPaymentChannel().getId())
                .paymentChannelName(t.getPaymentChannel().getName())
                .paymentGatewayId(t.getPaymentGateway().getId())
                .paymentGatewayName(t.getPaymentGateway().getName())
                .businessType(t.getBusinessType())
                .businessReference(t.getBusinessReference())
                .currency(t.getCurrency())
                .amount(t.getAmount())
                .status(t.getStatus())
                .requestedAt(t.getRequestedAt())
                .completedAt(t.getCompletedAt())
                .items(items)
                .build();
    }
}
