package com.example.loan.dto;

public enum EligibilityResult {
    PASS,
    FAIL,
    // The rule's field has no source data anywhere in the system (e.g. CREDIT_SCORE,
    // DEBT_TO_INCOME_RATIO) or the customer has no record for a field that does
    // (e.g. no employment on file for an EMPLOYMENT_STATUS rule) — not evaluated
    // either way, distinct from a rule that was evaluated and failed.
    NOT_EVALUABLE
}
