package com.example.notification.service.impl;

import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationChannel;
import com.example.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;

// The single NotificationSender bean — dispatches by channel instead of each
// channel registering its own NotificationSender, which would leave Spring
// with an ambiguous bean to inject into NotificationServiceImpl.
//
// Only EMAIL is real (via EmailSender/Mailhog in dev). SMS/PUSH/IN_APP, and
// EMAIL with no recipientContact, fall back to logging what would have gone
// out and reporting success — same behavior this class had before it was
// LoggingNotificationSender for every channel.
@Component
@RequiredArgsConstructor
public class DefaultNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationSender.class);

    private final EmailSender emailSender;

    @Override
    public String send(Notification notification) {
        if (notification.getChannel() == NotificationChannel.EMAIL
                && StringUtils.hasText(notification.getRecipientContact())) {
            return emailSender.send(
                    notification.getRecipientContact(), notification.getSubject(), notification.getMessage());
        }

        log.info("Notification [{} -> {}:{}] subject=\"{}\" message=\"{}\"",
                notification.getChannel(),
                notification.getRecipientType(),
                notification.getRecipientId(),
                notification.getSubject(),
                notification.getMessage());
        return null;
    }
}
