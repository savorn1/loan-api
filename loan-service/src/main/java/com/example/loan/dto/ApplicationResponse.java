package com.example.loan.dto;

import com.example.loan.entity.ApplicationStatus;
import com.example.loan.entity.TermUnit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ApplicationResponse {

    private Long id;
    private String applicationNo;
    private Long customerId;
    private String customerName;
    private Long branchId;
    private UUID loanProductId;
    private String loanProductName;
    private BigDecimal requestedAmount;
    private Integer requestedTermMonths;
    private TermUnit requestedTermUnit;
    private String purpose;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;
    private Long loanId;
    private List<ApplicationDocumentResponse> documents;
    private List<ApplicationNoteResponse> notes;
    private List<ApplicationApprovalResponse> approvals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
