package com.example.notification.service.impl;

import com.example.notification.entity.Notification;
import com.example.notification.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// The only NotificationSender implementation right now: no email/SMS/push
// provider is configured anywhere in this stack, so "sending" just means
// logging what would have gone out and reporting success. See
// NotificationSender for how to replace this with a real integration later.
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public String send(Notification notification) {
        log.info("Notification [{} -> {}:{}] subject=\"{}\" message=\"{}\"",
                notification.getChannel(),
                notification.getRecipientType(),
                notification.getRecipientId(),
                notification.getSubject(),
                notification.getMessage());
        return null;
    }
}
