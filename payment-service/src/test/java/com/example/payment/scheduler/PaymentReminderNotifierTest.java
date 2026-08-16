package com.example.payment.scheduler;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.client.NotificationClient;
import com.example.payment.common.ApiResponse;
import com.example.payment.dto.CustomerResponse;
import com.example.payment.dto.LoanResponse;
import com.example.payment.dto.NotificationChannel;
import com.example.payment.dto.NotificationRequest;
import com.example.payment.entity.Payment;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Regression coverage for reminders reaching every channel a customer has
// contact info for, not just email — see DefaultNotificationSender/SmsSender
// on the notification-service side for the other half of this fix.
@ExtendWith(MockitoExtension.class)
class PaymentReminderNotifierTest {

    @Mock private LoanClient loanClient;
    @Mock private CustomerClient customerClient;
    @Mock private NotificationClient notificationClient;

    private PaymentReminderNotifier notifier;
    private Payment payment;

    @BeforeEach
    void setUp() {
        notifier = new PaymentReminderNotifier(loanClient, customerClient, notificationClient);

        payment = Payment.builder()
                .loanId(5L).amount(new BigDecimal("50.00")).dueDate(LocalDate.now()).build();
        payment.setId(1L);

        LoanResponse loan = new LoanResponse();
        loan.setCustomerId(9L);
        when(loanClient.getById(5L)).thenReturn(ApiResponse.success(loan));
    }

    @Test
    void notify_sendsBothEmailAndSmsWhenCustomerHasBoth() {
        CustomerResponse customer = new CustomerResponse();
        customer.setEmail("dara@example.com");
        customer.setPhone("+855123456789");
        when(customerClient.getById(9L)).thenReturn(ApiResponse.success(customer));

        notifier.notify(payment, "Payment due soon", "Your payment is due.");

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationClient, org.mockito.Mockito.times(2)).create(captor.capture());
        List<NotificationRequest> requests = captor.getAllValues();

        assertThat(requests).anySatisfy(r -> {
            assertThat(r.getChannel()).isEqualTo(NotificationChannel.EMAIL);
            assertThat(r.getRecipientContact()).isEqualTo("dara@example.com");
        });
        assertThat(requests).anySatisfy(r -> {
            assertThat(r.getChannel()).isEqualTo(NotificationChannel.SMS);
            assertThat(r.getRecipientContact()).isEqualTo("+855123456789");
        });
    }

    @Test
    void notify_sendsOnlyEmailWhenCustomerHasNoPhone() {
        CustomerResponse customer = new CustomerResponse();
        customer.setEmail("dara@example.com");
        when(customerClient.getById(9L)).thenReturn(ApiResponse.success(customer));

        notifier.notify(payment, "Payment due soon", "Your payment is due.");

        verify(notificationClient).create(any());
    }

    @Test
    void notify_sendsNothingWhenCustomerHasNeitherContact() {
        when(customerClient.getById(9L)).thenReturn(ApiResponse.success(new CustomerResponse()));

        notifier.notify(payment, "Payment due soon", "Your payment is due.");

        verify(notificationClient, never()).create(any());
    }
}
