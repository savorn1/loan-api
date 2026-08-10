package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DataQualityExceptionRow {

    // "LOAN" or "APPLICATION"
    private String entityType;
    private Long entityId;
    // loanNo/applicationNo when set, otherwise "#<id>" as a fallback — the report
    // exists partly *because* that identifier can be missing.
    private String identifier;
    private String issueType;
    private String description;
    private LocalDateTime recordCreatedAt;
}
