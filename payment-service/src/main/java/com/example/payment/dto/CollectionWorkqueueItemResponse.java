package com.example.payment.dto;

import com.example.payment.entity.CollectionCaseStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectionWorkqueueItemResponse {

    private Long loanId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private BigDecimal principal;
    private BigDecimal outstandingBalance;
    private BigDecimal totalOverdueAmount;
    private LocalDate oldestDueDate;
    private long maxDpd;
    private CollectionBucket bucket;
    private int overdueInstallmentCount;
    // Null when no CollectionCase exists yet for this loan (nobody has assigned,
    // changed status, or added a note) — the frontend renders that as "unassigned".
    private CollectionCaseStatus caseStatus;
    private Long assignedToUserId;
    private LocalDateTime lastContactAt;
    private LocalDate nextFollowUpAt;
}
