package com.example.payment.dto;

// Mirrors notification-service's own RecipientType — payment-service only
// ever sends CUSTOMER-targeted reminders, but keeps this a proper enum
// (rather than a raw string literal) so a typo can't silently mismatch what
// notification-service expects.
public enum RecipientType {
    CUSTOMER,
    USER
}
