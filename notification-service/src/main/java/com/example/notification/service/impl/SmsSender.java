package com.example.notification.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Mirrors EmailSender's shape — kept as its own component so it's a clean,
// single-purpose seam to drop a real SMS gateway (Twilio, etc.) into later.
// No gateway is configured yet, so this only logs what would have been sent;
// same "never throws" contract as EmailSender, just without an outbound call
// behind it.
@Component
public class SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);

    public String send(String to, String body) {
        log.info("SMS to {}: {}", to, body);
        return null;
    }
}
