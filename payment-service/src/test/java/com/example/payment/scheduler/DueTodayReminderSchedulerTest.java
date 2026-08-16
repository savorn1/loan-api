package com.example.payment.scheduler;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DueTodayReminderSchedulerTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentReminderNotifier reminderNotifier;

    @Test
    void sendDueTodayReminders_notifiesEveryPaymentDueToday() {
        Payment payment = Payment.builder()
                .loanId(5L).amount(new BigDecimal("50.00")).dueDate(LocalDate.now())
                .status(PaymentStatus.PENDING).build();
        payment.setId(1L);
        when(paymentRepository.findByDueDateAndStatus(LocalDate.now(), PaymentStatus.PENDING))
                .thenReturn(List.of(payment));

        new DueTodayReminderScheduler(paymentRepository, reminderNotifier).sendDueTodayReminders();

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(reminderNotifier).notify(eq(payment), subjectCaptor.capture(), any());
        assertThat(subjectCaptor.getValue()).isEqualTo("Payment due today");
    }
}
