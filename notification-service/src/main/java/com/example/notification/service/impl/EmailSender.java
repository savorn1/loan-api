package com.example.notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

// Thin wrapper around JavaMailSender — kept separate from
// DefaultNotificationSender so the "which channel goes where" dispatch logic
// stays readable and this class only ever has to know how to send one email.
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.from}")
    private String from;

    // Never throws — catches MailException and returns its message so the
    // caller can record a FAILED notification instead of blowing up the
    // request (same contract as NotificationSender.send itself).
    public String send(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(to);
            mail.setSubject(subject != null ? subject : "");
            mail.setText(body);
            mailSender.send(mail);
            return null;
        } catch (MailException ex) {
            return ex.getMessage();
        }
    }
}
