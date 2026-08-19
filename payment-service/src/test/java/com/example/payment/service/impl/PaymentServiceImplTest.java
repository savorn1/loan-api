package com.example.payment.service.impl;

import com.example.payment.client.LoanClient;
import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.entity.PaymentStatusHistory;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.PaymentStatusHistoryRepository;
import com.example.payment.scheduler.PaymentReminderNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    @Mock private LoanClient loanClient;
    @Mock private PaymentReminderNotifier reminderNotifier;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(
                paymentRepository, paymentStatusHistoryRepository, loanClient, reminderNotifier);
    }

    @Test
    void markAsPaid_notifiesCustomerPaymentWasReceived() {
        Payment payment = Payment.builder()
                .loanId(5L).amount(new BigDecimal("50.00")).dueDate(LocalDate.now())
                .status(PaymentStatus.PENDING).build();
        payment.setId(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markAsPaid(1L, "kim.dara");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(reminderNotifier).notify(any(Payment.class), subjectCaptor.capture(), any());
        assertThat(subjectCaptor.getValue()).isEqualTo("Payment received");
    }

    @Test
    void markAsPaid_recordsWhoPaidItAndAppendsStatusHistory() {
        Payment payment = Payment.builder()
                .loanId(5L).amount(new BigDecimal("50.00")).dueDate(LocalDate.now())
                .status(PaymentStatus.PENDING).build();
        payment.setId(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.markAsPaid(1L, "kim.dara");

        assertThat(payment.getPaidBy()).isEqualTo("kim.dara");

        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);
        verify(paymentStatusHistoryRepository).save(historyCaptor.capture());
        PaymentStatusHistory history = historyCaptor.getValue();
        assertThat(history.getFromStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(history.getToStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(history.getChangedBy()).isEqualTo("kim.dara");
        assertThat(history.getPayment()).isSameAs(payment);
    }
}
