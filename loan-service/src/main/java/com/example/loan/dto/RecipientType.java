package com.example.loan.dto;

// Mirrors notification-service's own RecipientType — loan-service only ever
// sends CUSTOMER-targeted notifications, but keeps this a proper enum
// (rather than a raw string literal) so a typo can't silently mismatch what
// notification-service expects.
public enum RecipientType {
    CUSTOMER,
    USER
}
