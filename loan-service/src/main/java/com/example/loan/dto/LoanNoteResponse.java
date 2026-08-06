package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoanNoteResponse {

    private Long id;
    private Long loanId;
    private String authorName;
    private String note;
    private LocalDateTime createdAt;
}
