package com.example.loan.dto;

import lombok.Data;

// Subset of accounting-service's JournalEntryResponse — loan-service only needs enough
// to log/trace what was posted, not the full line breakdown.
@Data
public class JournalEntryResponse {

    private Long id;
    private String entryNo;
    private String status;
}
