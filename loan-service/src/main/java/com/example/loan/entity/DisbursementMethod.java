package com.example.loan.entity;

public enum DisbursementMethod {
    BANK_TRANSFER,
    CASH,
    CHEQUE,
    MOBILE_WALLET,
    // Used where a payment is recorded without channel info — e.g. LoanServiceImpl's
    // legacy applyPayment() action, called from payment-service with just an amount.
    OTHER
}
