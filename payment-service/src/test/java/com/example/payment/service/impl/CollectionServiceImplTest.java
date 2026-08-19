package com.example.payment.service.impl;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.common.ApiResponse;
import com.example.payment.dto.CustomerResponse;
import com.example.payment.dto.LoanResponse;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.CollectionActivityRepository;
import com.example.payment.repository.CollectionCaseAssignmentRepository;
import com.example.payment.repository.CollectionCaseRepository;
import com.example.payment.repository.CollectionLetterRepository;
import com.example.payment.repository.CollectionPromiseRepository;
import com.example.payment.repository.CollectionStatusHistoryRepository;
import com.example.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private CollectionCaseRepository collectionCaseRepository;
    @Mock private CollectionActivityRepository collectionActivityRepository;
    @Mock private CollectionStatusHistoryRepository collectionStatusHistoryRepository;
    @Mock private CollectionCaseAssignmentRepository collectionCaseAssignmentRepository;
    @Mock private CollectionPromiseRepository collectionPromiseRepository;
    @Mock private CollectionLetterRepository collectionLetterRepository;
    @Mock private LoanClient loanClient;
    @Mock private CustomerClient customerClient;

    private CollectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CollectionServiceImpl(
                paymentRepository, collectionCaseRepository, collectionActivityRepository,
                collectionStatusHistoryRepository, collectionCaseAssignmentRepository,
                collectionPromiseRepository, collectionLetterRepository, loanClient, customerClient);

        LoanResponse loan = new LoanResponse();
        loan.setId(7L);
        loan.setCustomerId(9L);
        loan.setCustomerName("Jane Doe");
        loan.setPrincipal(new BigDecimal("1000.00"));
        loan.setOutstandingBalance(new BigDecimal("500.00"));
        loan.setStatus("ACTIVE");
        lenient().when(loanClient.getById(7L)).thenReturn(ApiResponse.success(loan));
        lenient().when(customerClient.getById(9L)).thenReturn(ApiResponse.success(new CustomerResponse()));
        lenient().when(collectionCaseRepository.findByLoanId(anyLong())).thenReturn(java.util.Optional.empty());
    }

    // ── getLiveOverdueLoans: sources from due dates, not the OVERDUE status flag ───

    @Test
    void getLiveOverdueLoans_includesAPastDuePaymentStillFlaggedPending() {
        Payment pastDuePending = Payment.builder()
                .loanId(7L).amount(new BigDecimal("50.00"))
                .dueDate(LocalDate.now().minusDays(5)).status(PaymentStatus.PENDING).build();
        when(paymentRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), any(PaymentStatus.class)))
                .thenReturn(List.of(pastDuePending));

        var result = service.getLiveOverdueLoans(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLoanId()).isEqualTo(7L);
        assertThat(result.get(0).getMaxDpd()).isEqualTo(5);
        verify(paymentRepository, never()).findByStatus(any());
    }

    @Test
    void getLiveOverdueLoans_queriesStrictlyBeforeTodayExcludingOnlyPaid() {
        when(paymentRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), any(PaymentStatus.class)))
                .thenReturn(List.of());

        service.getLiveOverdueLoans(null, null);

        verify(paymentRepository).findByDueDateBeforeAndStatusNot(LocalDate.now(), PaymentStatus.PAID);
    }

    // ── getWorkqueue: unchanged behavior after the refactor — still status-based ───

    @Test
    void getWorkqueue_stillSourcesFromTheOverdueStatusFlag() {
        when(paymentRepository.findByStatus(PaymentStatus.OVERDUE)).thenReturn(List.of());

        service.getWorkqueue(null, null);

        verify(paymentRepository).findByStatus(PaymentStatus.OVERDUE);
        verify(paymentRepository, never()).findByDueDateBeforeAndStatusNot(any(), any());
    }
}
