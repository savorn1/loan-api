package com.example.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationNoteResponse {

    private Long id;
    private Long applicationId;
    private String authorName;
    private String note;
    private LocalDateTime createdAt;
}
