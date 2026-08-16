package com.example.payment.scheduler;

import com.example.payment.client.CustomerClient;
import com.example.payment.client.LoanClient;
import com.example.payment.client.NotificationClient;
import com.example.payment.dto.CustomerResponse;
import com.example.payment.dto.LoanResponse;
import com.example.payment.dto.NotificationChannel;
import com.example.payment.dto.NotificationRequest;
import com.example.payment.dto.RecipientType;
import com.example.payment.entity.Payment;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// Shared by both reminder schedulers (due-soon and overdue): resolves a
// payment's loan -> customer, then calls notification-service on every
// channel the customer has contact info for (email and/or SMS) rather than
// email alone. One bad Feign call (or a customer with neither on file) skips
// just that payment instead of failing the whole scheduled batch — same
// defensive per-row pattern as CollectionServiceImpl's fetchLoan/fetchCustomer.
@Component
@RequiredArgsConstructor
public class PaymentReminderNotifier {

    private static final Logger log = LoggerFactory.getLogger(PaymentReminderNotifier.class);

    private final LoanClient loanClient;
    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    public void notify(Payment payment, String subject, String message) {
        try {
            LoanResponse loan = loanClient.getById(payment.getLoanId()).getData();
            if (loan == null) return;

            CustomerResponse customer = customerClient.getById(loan.getCustomerId()).getData();
            if (customer == null) return;

            if (StringUtils.hasText(customer.getEmail())) {
                send(loan.getCustomerId(), NotificationChannel.EMAIL, customer.getEmail(), subject, message);
            }
            if (StringUtils.hasText(customer.getPhone())) {
                send(loan.getCustomerId(), NotificationChannel.SMS, customer.getPhone(), subject, message);
            }
        } catch (FeignException ex) {
            log.warn("Skipping reminder for payment {}: {}", payment.getId(), ex.getMessage());
        }
    }

    private void send(Long customerId, NotificationChannel channel, String contact, String subject, String message) {
        notificationClient.create(NotificationRequest.builder()
                .recipientType(RecipientType.CUSTOMER)
                .recipientId(customerId)
                .channel(channel)
                .recipientContact(contact)
                .subject(subject)
                .message(message)
                .build());
    }
}
