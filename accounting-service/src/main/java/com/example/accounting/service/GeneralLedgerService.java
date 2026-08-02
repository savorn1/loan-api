package com.example.accounting.service;

import java.time.LocalDate;
import java.util.List;

import com.example.accounting.dto.BranchLedgerResponse;
import com.example.accounting.dto.DateRangeLedgerResponse;
import com.example.accounting.dto.GeneralLedgerResponse;
import com.example.accounting.dto.GeneralLedgerSummaryRow;
import com.example.accounting.entity.JournalEntry;

public interface GeneralLedgerService {

    GeneralLedgerResponse getLedger(Long glAccountId, Long financialPeriodId);
    void applyPostedEntry(JournalEntry entry);
    List<GeneralLedgerSummaryRow> getAllLedgers(Long financialPeriodId);
    DateRangeLedgerResponse getLedgerForDateRange(Long glAccountId, LocalDate dateFrom, LocalDate dateTo);
    BranchLedgerResponse getBranchLedger(Long branchId, LocalDate dateFrom, LocalDate dateTo);
}
