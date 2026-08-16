package com.example.customer.dto;

// Mirrors notification-service's own RecipientType — customer-service only
// ever sends CUSTOMER-targeted notifications, but keeps this a proper enum
// (rather than a raw string literal) so a typo can't silently mismatch what
// notification-service expects.
public enum RecipientType {
    CUSTOMER,
    USER
}
