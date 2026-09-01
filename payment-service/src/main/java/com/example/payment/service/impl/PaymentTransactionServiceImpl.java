package com.example.payment.service.impl;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.client.PaymentWalletClient;
import com.example.payment.config.PaymentTransactionDefaultsSeeder;
import com.example.payment.dto.ApplyPaymentRequest;
import com.example.payment.dto.CustomerResponse;
import com.example.payment.dto.PaymentTransactionItemResponse;
import com.example.payment.dto.PaymentTransactionRequest;
import com.example.payment.dto.PaymentTransactionResponse;
import com.example.payment.dto.PaymentWalletDepositResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentTransactionItemRepository paymentTransactionItemRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentChannelRepository paymentChannelRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final CustomerClient customerClient;
    private final LoanClient loanClient;
    private final PaymentWalletClient paymentWalletClient;

    // PaymentGateway.provider value that routes create() through the payment-wallet
    // merchant API (POST /deposits) instead of leaving the transaction as a bare,
    // manually-settled PENDING record — see PaymentGateway.apiUrl for the target host.
    private static final String PROVIDER_PAYMENT_WALLET = "PAYMENT_WALLET";

    // Only these transitions are allowed — mirrors the two action states the
    // frontend's detail page offers (PENDING -> SUCCESS/FAILED, SUCCESS -> REFUNDED),
    // plus PENDING -> EXPIRED, which is system-driven only (payment-wallet's webhook
    // or PaymentWalletReconciliationScheduler) — no UI action sets it directly.
    private static final Map<TransactionStatus, Set<TransactionStatus>> ALLOWED_TRANSITIONS = Map.of(
            TransactionStatus.PENDING, EnumSet.of(TransactionStatus.SUCCESS, TransactionStatus.FAILED, TransactionStatus.EXPIRED),
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
        if (StringUtils.hasText(request.getIdempotencyKey())) {
            Optional<PaymentTransaction> existing =
                    paymentTransactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                PaymentTransaction transaction = existing.get();
                return toResponse(transaction, resolveCustomerName(transaction.getCustomerId()), itemsFor(transaction.getId()));
            }
        }

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

        // Called before any local row is persisted: if payment-wallet is unreachable or
        // rejects the request, create() fails outright rather than leaving behind a
        // PENDING transaction with no way to actually collect the money. Reuses the
        // caller's own idempotencyKey against payment-wallet when given, so a retried
        // create() call (e.g. after a client timeout) can't open a second deposit even
        // if it somehow raced past the findByIdempotencyKey lookup above.
        String walletIdempotencyKey = StringUtils.hasText(request.getIdempotencyKey())
                ? request.getIdempotencyKey()
                : UUID.randomUUID().toString();
        PaymentWalletDepositResponse deposit = isPaymentWalletGateway(gateway)
                ? paymentWalletClient.createDeposit(gateway.getApiUrl(), request.getAmount(), walletIdempotencyKey)
                : null;

        PaymentTransaction.PaymentTransactionBuilder transactionBuilder = PaymentTransaction.builder()
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
                .idempotencyKey(request.getIdempotencyKey())
                .status(TransactionStatus.PENDING)
                .requestedAt(LocalDateTime.now());
        if (deposit != null) {
            transactionBuilder
                    .walletDepositId(deposit.getId())
                    .providerReference(deposit.getProviderReference())
                    .paymentUrl(deposit.getPaymentUrl())
                    .walletExpiresAt(deposit.getExpiresAt());
        }
        PaymentTransaction transaction = paymentTransactionRepository.save(transactionBuilder.build());
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
        return paymentTransactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
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
        // Applied before the status flip below: if the loan is no longer ACTIVE (or
        // loan-service is unreachable), this throws and the transaction stays PENDING
        // rather than reporting SUCCESS with nothing actually applied to the loan —
        // the wallet webhook retries on a non-2xx response, and a manual retry from
        // the UI has the same effect.
        if (status == TransactionStatus.SUCCESS && BUSINESS_TYPE_LOAN_PAYMENT.equals(transaction.getBusinessType())) {
            applyToLoan(transaction);
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

    // Reduces the loan's outstanding balance by this transaction's amount and, via
    // loan-service's applyPayment -> recordTransaction, posts the corresponding
    // journal entry to accounting — the same chokepoint PaymentServiceImpl.markAsPaid
    // uses for installment-level payments. recordTransaction=false: this
    // PaymentTransaction row already exists, so loan-service shouldn't synthesize
    // another one for it (see ApplyPaymentRequest.recordTransaction).
    private void applyToLoan(PaymentTransaction transaction) {
        Long loanId = parseReferenceId(transaction.getBusinessReference());
        ApplyPaymentRequest applyRequest = new ApplyPaymentRequest();
        applyRequest.setAmount(transaction.getAmount());
        applyRequest.setRecordTransaction(false);
        loanClient.applyPayment(loanId, applyRequest);
    }

    @Override
    public void applyWalletDepositWebhook(Long walletDepositId, String walletStatus, String failureReason) {
        Optional<PaymentTransaction> transaction = paymentTransactionRepository.findByWalletDepositId(walletDepositId);
        if (transaction.isEmpty()) {
            log.warn("Received payment-wallet webhook for unknown deposit id {}", walletDepositId);
            return;
        }
        TransactionStatus target = mapWalletStatus(walletStatus);
        if (target == null || transaction.get().getStatus() == target) {
            // Unrecognized/non-terminal status, or a replayed delivery we already applied
            // — see the "Replay awareness" callout in payment-wallet's webhook docs.
            return;
        }
        String reason = target == TransactionStatus.REFUNDED && !StringUtils.hasText(failureReason)
                ? "Refunded via payment-wallet"
                : failureReason;
        updateStatus(transaction.get().getId(), target, reason, "payment-wallet-webhook");
    }

    private TransactionStatus mapWalletStatus(String walletStatus) {
        if (walletStatus == null) {
            return null;
        }
        return switch (walletStatus) {
            case "SUCCESS" -> TransactionStatus.SUCCESS;
            case "FAILED" -> TransactionStatus.FAILED;
            case "REFUNDED" -> TransactionStatus.REFUNDED;
            case "EXPIRED" -> TransactionStatus.EXPIRED;
            default -> null;
        };
    }

    private boolean isPaymentWalletGateway(PaymentGateway gateway) {
        return PROVIDER_PAYMENT_WALLET.equalsIgnoreCase(gateway.getProvider());
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
                .providerReference(t.getProviderReference())
                .paymentUrl(t.getPaymentUrl())
                .walletExpiresAt(t.getWalletExpiresAt())
                .items(items)
                .build();
    }
}
