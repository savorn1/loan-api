package com.example.accounting.entity;

// Shared by GlAccount.normalBalance and by each journal line's own side —
// a line posts on one side, an account's normal balance says which side increases it.
public enum EntrySide {
    DEBIT,
    CREDIT
}
