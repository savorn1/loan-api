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
// payment's loan -> customer to get an email address, then calls
// notification-service. One bad Feign call (or a customer with no email on
// file) skips just that payment instead of failing the whole scheduled batch
// — same defensive per-row pattern as CollectionServiceImpl's
// fetchLoan/fetchCustomer.
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
            if (customer == null || !StringUtils.hasText(customer.getEmail())) return;

            notificationClient.create(NotificationRequest.builder()
                    .recipientType(RecipientType.CUSTOMER)
                    .recipientId(loan.getCustomerId())
                    .channel(NotificationChannel.EMAIL)
                    .recipientContact(customer.getEmail())
                    .subject(subject)
                    .message(message)
                    .build());
        } catch (FeignException ex) {
            log.warn("Skipping reminder for payment {}: {}", payment.getId(), ex.getMessage());
        }
    }
}
