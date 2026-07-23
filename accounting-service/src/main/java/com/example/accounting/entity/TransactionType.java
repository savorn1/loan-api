package com.example.accounting.entity;

// The only vocabulary this service shares with loan-service/payment-service — a business
// event name, never a loan product id or loan-domain concept. Each type is resolved to actual
// GL accounts via a JournalTemplate + AccountingScheme, not hardcoded here.
public enum TransactionType {
    DISBURSEMENT,
    PRINCIPAL_PAYMENT,
    INTEREST_PAYMENT,
    FEE_CHARGE,
    PENALTY_CHARGE,
    LOAN_WRITE_OFF,
    PAYMENT_REVERSAL
}
