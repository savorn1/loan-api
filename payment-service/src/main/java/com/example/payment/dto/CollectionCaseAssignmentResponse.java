package com.example.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CollectionCaseAssignmentResponse {

    private Long id;
    private Long loanId;
    private Long assignedToUserId;
    private String assignedBy;
    private String note;
    private LocalDateTime assignedAt;
}
