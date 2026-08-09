package com.example.accounting.entity;
public enum TransactionType {
    DISBURSEMENT,
    PRINCIPAL_PAYMENT,
    INTEREST_PAYMENT,
    FEE_CHARGE,
    PENALTY_CHARGE,
    LOAN_WRITE_OFF,
    PAYMENT_REVERSAL,
    // Cash recovered on a loan that was already written off — recognized as income (Bad Debt
    // Recovery), not a reversal of the original LOAN_WRITE_OFF entry, which stays posted as
    // the historical record of the charge-off itself.
    RECOVERY
}
