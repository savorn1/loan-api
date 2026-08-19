package com.example.payment.service.impl;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.common.ApiResponse;
import com.example.payment.config.PaymentTransactionDefaultsSeeder;
import com.example.payment.dto.CustomerResponse;
import com.example.payment.dto.LoanResponse;
import com.example.payment.dto.PaymentTransactionRequest;
import com.example.payment.dto.PaymentTransactionResponse;
import com.example.payment.entity.PaymentChannel;
import com.example.payment.entity.PaymentGateway;
import com.example.payment.entity.PaymentMethod;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.entity.TransactionStatus;
import com.example.payment.exception.AppException;
import com.example.payment.repository.PaymentChannelRepository;
import com.example.payment.repository.PaymentGatewayRepository;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.repository.PaymentTransactionItemRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceImplTest {

    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private PaymentTransactionItemRepository paymentTransactionItemRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private PaymentChannelRepository paymentChannelRepository;
    @Mock private PaymentGatewayRepository paymentGatewayRepository;
    @Mock private CustomerClient customerClient;
    @Mock private LoanClient loanClient;

    private PaymentTransactionServiceImpl service;
    private PaymentTransaction transaction;

    @BeforeEach
    void setUp() {
        service = new PaymentTransactionServiceImpl(paymentTransactionRepository, paymentTransactionItemRepository,
                paymentMethodRepository, paymentChannelRepository, paymentGatewayRepository, customerClient,
                loanClient);

        PaymentMethod method = PaymentMethod.builder().id(1L).name("Cash").build();
        PaymentChannel channel = PaymentChannel.builder().id(1L).name("Branch").build();
        PaymentGateway gateway = PaymentGateway.builder().id(1L).name("Internal").build();

        transaction = PaymentTransaction.builder()
                .customerId(9L).paymentMethod(method).paymentChannel(channel).paymentGateway(gateway)
                .businessType("LOAN_PAYMENT").currency("USD").amount(new BigDecimal("50.00"))
                .status(TransactionStatus.SUCCESS).requestedAt(LocalDateTime.now()).build();
        transaction.setId(1L);

        lenient().when(paymentTransactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        lenient().when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        lenient().when(customerClient.getById(9L)).thenReturn(ApiResponse.success(new CustomerResponse()));
        lenient().when(paymentTransactionItemRepository.save(any())).thenAnswer(inv -> {
            var item = (com.example.payment.entity.PaymentTransactionItem) inv.getArgument(0);
            item.setId(1L);
            return item;
        });
    }

    @Test
    void updateStatus_rejectsRefundWithoutAReason() {
        assertThatThrownBy(() -> service.updateStatus(1L, TransactionStatus.REFUNDED, "  ", "kim.dara"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateStatus_refundRecordsWhoAndWhyAndWhen() {
        PaymentTransactionResponse response =
                service.updateStatus(1L, TransactionStatus.REFUNDED, "Duplicate charge", "kim.dara");

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.REFUNDED);
        assertThat(response.getRefundedBy()).isEqualTo("kim.dara");
        assertThat(response.getRefundReason()).isEqualTo("Duplicate charge");
        assertThat(response.getRefundedAt()).isNotNull();
    }

    @Test
    void updateStatus_rejectsTransitionNotInTheAllowList() {
        transaction.setStatus(TransactionStatus.FAILED);

        assertThatThrownBy(() -> service.updateStatus(1L, TransactionStatus.REFUNDED, "why not", "kim.dara"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateStatus_rejectsRefundOfAnAutoCreatedLoanRepaymentTransaction() {
        transaction.setPaymentMethod(PaymentMethod.builder()
                .id(1L).code(PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE).name("Internal / Manual").build());

        assertThatThrownBy(() -> service.updateStatus(1L, TransactionStatus.REFUNDED, "why not", "kim.dara"))
                .isInstanceOf(AppException.class);
    }

    // ── createForLoanRepayment: books an already-completed loan payment ────────────────

    @Test
    void createForLoanRepayment_bookedAsAnImmediateSuccessAgainstTheInternalDefaults() {
        PaymentMethod method = PaymentMethod.builder()
                .id(1L).code(PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE).name("Internal / Manual").build();
        PaymentChannel channel = PaymentChannel.builder()
                .id(1L).code(PaymentTransactionDefaultsSeeder.INTERNAL_CHANNEL_CODE).name("Loan Repayment").build();
        PaymentGateway gateway = PaymentGateway.builder()
                .id(1L).code(PaymentTransactionDefaultsSeeder.INTERNAL_GATEWAY_CODE).name("Internal").build();
        when(paymentMethodRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE))
                .thenReturn(Optional.of(method));
        when(paymentChannelRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_CHANNEL_CODE))
                .thenReturn(Optional.of(channel));
        when(paymentGatewayRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_GATEWAY_CODE))
                .thenReturn(Optional.of(gateway));
        when(customerClient.getById(9L)).thenReturn(ApiResponse.success(new CustomerResponse()));

        PaymentTransactionResponse response = service.createForLoanRepayment(9L, 42L, new BigDecimal("50.00"));

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getBusinessType()).isEqualTo("LOAN_PAYMENT");
        assertThat(response.getBusinessReference()).isEqualTo("42");
        assertThat(response.getAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void createForLoanRepayment_failsClearlyWhenTheInternalDefaultsWereNeverSeeded() {
        when(paymentMethodRepository.findByCode(PaymentTransactionDefaultsSeeder.INTERNAL_METHOD_CODE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createForLoanRepayment(9L, 42L, new BigDecimal("50.00")))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── create: businessReference must be a real, checkable loan ───────────────────────

    @Test
    void create_confirmsTheReferencedLoanExistsWhenBusinessTypeIsLoanPayment() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(
                PaymentMethod.builder().id(1L).name("Cash").build()));
        when(paymentChannelRepository.findById(1L)).thenReturn(Optional.of(
                PaymentChannel.builder().id(1L).name("Branch").build()));
        when(paymentGatewayRepository.findById(1L)).thenReturn(Optional.of(
                PaymentGateway.builder().id(1L).name("Internal").build()));
        when(loanClient.getById(42L)).thenReturn(ApiResponse.success(new LoanResponse()));

        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setCustomerId(9L);
        request.setPaymentMethodId(1L);
        request.setPaymentChannelId(1L);
        request.setPaymentGatewayId(1L);
        request.setBusinessType("LOAN_PAYMENT");
        request.setBusinessReference("42");
        request.setCurrency("USD");
        request.setAmount(new BigDecimal("50.00"));

        service.create(request);

        ArgumentCaptor<Long> loanIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(loanClient).getById(loanIdCaptor.capture());
        assertThat(loanIdCaptor.getValue()).isEqualTo(42L);
    }

    @Test
    void create_skipsTheLoanCheckForOtherBusinessTypes() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(
                PaymentMethod.builder().id(1L).name("Cash").build()));
        when(paymentChannelRepository.findById(1L)).thenReturn(Optional.of(
                PaymentChannel.builder().id(1L).name("Branch").build()));
        when(paymentGatewayRepository.findById(1L)).thenReturn(Optional.of(
                PaymentGateway.builder().id(1L).name("Internal").build()));

        PaymentTransactionRequest request = new PaymentTransactionRequest();
        request.setCustomerId(9L);
        request.setPaymentMethodId(1L);
        request.setPaymentChannelId(1L);
        request.setPaymentGatewayId(1L);
        request.setBusinessType("OTHER");
        request.setBusinessReference("42");
        request.setCurrency("USD");
        request.setAmount(new BigDecimal("50.00"));

        service.create(request);

        verify(loanClient, never()).getById(any());
    }
}
