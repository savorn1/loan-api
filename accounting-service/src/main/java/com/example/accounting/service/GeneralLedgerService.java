package com.example.accounting.service;

import com.example.accounting.dto.GeneralLedgerResponse;
import com.example.accounting.entity.JournalEntry;

public interface GeneralLedgerService {

    GeneralLedgerResponse getLedger(Long glAccountId, Long financialPeriodId);

    // Rolls every line of a just-posted (or just-reversed, since a reversal is created
    // already-POSTED) journal entry into the general_ledger balance table.
    void applyPostedEntry(JournalEntry entry);
}
