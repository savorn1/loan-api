package com.example.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CollectorProductivityRow {

    // Null means "unassigned cases" — grouped as its own row rather than dropped, so the
    // report also surfaces how much of the caseload has no owner yet.
    private Long assignedToUserId;
    private long caseCount;
    private long openCaseCount;
    private long resolvedCaseCount;
    private long paymentCount;
    private BigDecimal totalCollected;
}
