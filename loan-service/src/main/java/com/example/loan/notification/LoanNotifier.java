package com.example.loan.notification;

import com.example.loan.client.NotificationClient;
import com.example.loan.dto.CustomerResponse;
import com.example.loan.dto.NotificationChannel;
import com.example.loan.dto.NotificationRequest;
import com.example.loan.dto.RecipientType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// Notifies a customer on every channel they have contact info for (email
// and/or SMS) — same dual-channel approach as payment-service's
// PaymentReminderNotifier. Callers pass the CustomerResponse they already
// have in hand (every call site here already fetched it to build its own
// response DTO) rather than this re-fetching it. A bad Feign call to
// notification-service doesn't fail the caller's own transaction — it's
// swallowed and logged, so e.g. a disbursement still succeeds even if
// notification-service is down.
@Component
@RequiredArgsConstructor
public class LoanNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoanNotifier.class);

    private final NotificationClient notificationClient;

    public void notify(CustomerResponse customer, String subject, String message) {
        if (customer == null) {
            return;
        }
        try {
            if (StringUtils.hasText(customer.getEmail())) {
                send(customer.getId(), NotificationChannel.EMAIL, customer.getEmail(), subject, message);
            }
            if (StringUtils.hasText(customer.getPhone())) {
                send(customer.getId(), NotificationChannel.SMS, customer.getPhone(), subject, message);
            }
        } catch (FeignException ex) {
            log.warn("Skipping notification to customer {}: {}", customer.getId(), ex.getMessage());
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
