package com.example.payment.service.impl;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.config.PaymentTransactionDefaultsSeeder;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    private final LoanClient loanClient;

    // Only these transitions are allowed — mirrors the two action states the
    // frontend's detail page offers (PENDING -> SUCCESS/FAILED, SUCCESS -> REFUNDED).
    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS = Map.of(
            TransactionStatus.PENDING, EnumSet.of(TransactionStatus.SUCCESS, TransactionStatus.FAILED),
            TransactionStatus.SUCCESS, EnumSet.of(TransactionStatus.REFUNDED)
    );

    // The only business type the frontend offers today — see PaymentTransactionRequest
    // for why businessReference is required now that it's meant to be a real, checkable
    // link rather than an arbitrary string.
    private static final String BUSINESS_TYPE_LOAN_PAYMENT = "LOAN_PAYMENT";

    @Value("${payment.default-currency:USD}")
    private String defaultCurrency;

    @Override
    public PaymentTransactionResponse create(PaymentTransactionRequest request) {
        PaymentMethod method = findMethodOrThrow(request.getPaymentMethodId());
        PaymentChannel channel = findChannelOrThrow(request.getPaymentChannelId());
        PaymentGateway gateway = findGatewayOrThrow(request.getPaymentGatewayId());
        String customerName = resolveCustomerName(request.getCustomerId());

        Long referenceId = parseReferenceId(request.getBusinessReference());
        if (BUSINESS_TYPE_LOAN_PAYMENT.equals(request.getBusinessType())) {
            // Confirms this transaction is actually attached to a real loan rather than
            // an arbitrary/mistyped id — a missing loan surfaces as a 404 here (Feign
            // exceptions are mapped generically by GlobalExceptionHandler).
            loanClient.getById(referenceId);
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .customerId(request.getCustomerId())
                .paymentMethod(method)
                .paymentChannel(channel)
                .paymentGateway(gateway)
                .businessType(request.getBusinessType())
                .businessReference(request.getBusinessReference())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .principalAmount(request.getPrincipalAmount())
                .interestAmount(request.getInterestAmount())
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
                .referenceId(referenceId)
                .amount(request.getAmount())
                .build());

        return toResponse(transaction, customerName, List.of(toItemResponse(item)));
    }

    @Override
    public PaymentTransactionResponse createForLoanRepayment(Long customerId, Long loanId, BigDecimal amount) {
        PaymentMethod method = paymentMethodRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default payment method not seeded: " + PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE));
        PaymentChannel channel = paymentChannelRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_CHANNEL_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default payment channel not seeded: " + PaymentTransactionDefaultsSeeder.INTERNAL_CHANNEL_CODE));
        PaymentGateway gateway = paymentGatewayRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_GATEWAY_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default payment gateway not seeded: " + PaymentTransactionDefaultsSeeder.INTERNAL_GATEWAY_CODE));

        LocalDateTime now = LocalDateTime.now();
        PaymentTransaction transaction = PaymentTransaction.builder()
                .customerId(customerId)
                .paymentMethod(method)
                .paymentChannel(channel)
                .paymentGateway(gateway)
                .businessType(BUSINESS_TYPE_LOAN_PAYMENT)
                .businessReference(String.valueOf(loanId))
                .currency(defaultCurrency)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .requestedAt(now)
                .completedAt(now)
                .build();
        transaction = paymentTransactionRepository.save(transaction);
        transaction.setPaymentNo(generatePaymentNo(transaction.getId()));
        transaction = paymentTransactionRepository.save(transaction);

        PaymentTransactionItem item = paymentTransactionItemRepository.save(PaymentTransactionItem.builder()
                .paymentTransaction(transaction)
                .referenceType(BUSINESS_TYPE_LOAN_PAYMENT)
                .referenceId(loanId)
                .amount(amount)
                .build());

        return toResponse(transaction, resolveCustomerName(customerId), List.of(toItemResponse(item)));
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
    public PaymentTransactionResponse updateStatus(Long id, TransactionStatus status, String reason, String changedBy) {
        PaymentTransaction transaction = findOrThrow(id);
        Set<TransactionStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(transaction.getStatus(), Set.of());
        if (!allowed.contains(status)) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Cannot transition transaction from " + transaction.getStatus() + " to " + status);
        }
        if (status == TransactionStatus.REFUNDED && !StringUtils.hasText(reason)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "A reason is required to refund a transaction");
        }
        // Auto-created by createForLoanRepayment() against the fixed internal method — there's
        // no corresponding "unmark paid" on the loan side, so refunding here would desync this
        // ledger from the loan's actual balance with no way to reconcile. The loan's own
        // reverse-payment action is the correct way to undo one of these.
        if (status == TransactionStatus.REFUNDED
                && PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE.equals(transaction.getPaymentMethod().getCode())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "This transaction was recorded automatically from a loan repayment and cannot be refunded here "
                            + "— reverse the payment on the loan instead.");
        }
        transaction.setStatus(status);
        if (status == TransactionStatus.SUCCESS || status == TransactionStatus.FAILED) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        if (status == TransactionStatus.REFUNDED) {
            transaction.setRefundedBy(changedBy);
            transaction.setRefundedAt(LocalDateTime.now());
            transaction.setRefundReason(reason);
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
                .paymentMethodCode(t.getPaymentMethod().getCode())
                .paymentChannelId(t.getPaymentChannel().getId())
                .paymentChannelName(t.getPaymentChannel().getName())
                .paymentGatewayId(t.getPaymentGateway().getId())
                .paymentGatewayName(t.getPaymentGateway().getName())
                .businessType(t.getBusinessType())
                .businessReference(t.getBusinessReference())
                .currency(t.getCurrency())
                .amount(t.getAmount())
                .principalAmount(t.getPrincipalAmount())
                .interestAmount(t.getInterestAmount())
                .status(t.getStatus())
                .requestedAt(t.getRequestedAt())
                .completedAt(t.getCompletedAt())
                .refundedBy(t.getRefundedBy())
                .refundedAt(t.getRefundedAt())
                .refundReason(t.getRefundReason())
                .items(items)
                .build();
    }
}
