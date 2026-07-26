package com.example.notification.entity;

// Which system a recipientId refers to — CUSTOMER ids live in customer-service,
// USER ids live in auth-service. No FK to either: this service has no
// relationship/join to those services' tables, just an opaque cross-service id
// (same pattern as accounting-service's referenceType/referenceId).
public enum RecipientType {
    CUSTOMER,
    USER
}
