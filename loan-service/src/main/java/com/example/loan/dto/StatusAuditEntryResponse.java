package com.example.loan.dto;

import com.example.loan.entity.LoanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Same fields as LoanStatusHistoryResponse plus loanNo — that one is scoped to a single
// loan's own history page and has no need to identify the loan on each row; this one
// is a cross-loan report, so the loan has to be identifiable per row.
@Data
@Builder
public class StatusAuditEntryResponse {

    private Long loanId;
    private String loanNo;
    private LoanStatus fromStatus;
    private LoanStatus toStatus;
    private String note;
    private String changedBy;
    private LocalDateTime changedAt;
}
