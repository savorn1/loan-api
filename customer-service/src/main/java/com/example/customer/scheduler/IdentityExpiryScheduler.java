package com.example.customer.scheduler;

import com.example.customer.client.NotificationClient;
import com.example.customer.dto.NotificationChannel;
import com.example.customer.dto.NotificationRequest;
import com.example.customer.dto.RecipientType;
import com.example.customer.entity.Customer;
import com.example.customer.entity.CustomerIdentity;
import com.example.customer.entity.IdentityStatus;
import com.example.customer.repository.CustomerIdentityRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

// Flips ACTIVE identities past their expiryDate to EXPIRED and notifies the customer —
// IdentityStatus.EXPIRED existed as an enum value with nothing that ever set it until
// this. Mirrors payment-service's OverdueScheduler: notify for exactly the rows just
// flipped in this run, not everything currently EXPIRED, or a customer would get
// re-notified daily for as long as they leave the document unrenewed.
@Component
@RequiredArgsConstructor
public class IdentityExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(IdentityExpiryScheduler.class);

    private final CustomerIdentityRepository customerIdentityRepository;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void flagExpiredIdentities() {
        List<CustomerIdentity> expired = customerIdentityRepository
                .findByStatusAndExpiryDateBefore(IdentityStatus.ACTIVE, LocalDate.now());
        expired.forEach(identity -> identity.setStatus(IdentityStatus.EXPIRED));
        customerIdentityRepository.saveAll(expired);

        for (CustomerIdentity identity : expired) {
            notify(identity);
        }
    }

    private void notify(CustomerIdentity identity) {
        Customer customer = identity.getCustomer();
        String message = String.format("Your %s (%s) expired on %s. Please provide an updated document.",
                identity.getIdentityType(), identity.getIdentityNumber(), identity.getExpiryDate());
        try {
            if (StringUtils.hasText(customer.getEmail())) {
                send(customer.getId(), NotificationChannel.EMAIL, customer.getEmail(), message);
            }
            if (StringUtils.hasText(customer.getPhone())) {
                send(customer.getId(), NotificationChannel.SMS, customer.getPhone(), message);
            }
        } catch (FeignException ex) {
            log.warn("Skipping expiry notification for identity {}: {}", identity.getId(), ex.getMessage());
        }
    }

    private void send(Long customerId, NotificationChannel channel, String contact, String message) {
        notificationClient.create(NotificationRequest.builder()
                .recipientType(RecipientType.CUSTOMER)
                .recipientId(customerId)
                .channel(channel)
                .recipientContact(contact)
                .subject("Document expired")
                .message(message)
                .build());
    }
}
