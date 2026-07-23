package com.example.loan.dto;

import com.example.loan.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApplicationResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal requestedAmount;
    private Integer requestedTermMonths;
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
