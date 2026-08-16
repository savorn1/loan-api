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
// EMAIL (via EmailSender/Mailhog in dev) and SMS (via SmsSender, log-only —
// no gateway configured) each have a dedicated sender. PUSH/IN_APP, and
// either channel with no recipientContact, fall back to logging what would
// have gone out and reporting success.
@Component
@RequiredArgsConstructor
public class DefaultNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationSender.class);

    private final EmailSender emailSender;
    private final SmsSender smsSender;

    @Override
    public String send(Notification notification) {
        if (StringUtils.hasText(notification.getRecipientContact())) {
            if (notification.getChannel() == NotificationChannel.EMAIL) {
                return emailSender.send(
                        notification.getRecipientContact(), notification.getSubject(), notification.getMessage());
            }
            if (notification.getChannel() == NotificationChannel.SMS) {
                return smsSender.send(notification.getRecipientContact(), notification.getMessage());
            }
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
