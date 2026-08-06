package com.example.loan.dto;

import com.example.loan.entity.LoanDocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoanDocumentResponse {

    private Long id;
    private Long loanId;
    private String name;
    private LoanDocumentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
