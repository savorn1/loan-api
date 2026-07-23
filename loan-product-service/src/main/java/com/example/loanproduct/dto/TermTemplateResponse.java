package com.example.loanproduct.dto;

import com.example.loanproduct.entity.TermTemplateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TermTemplateResponse {

    private UUID id;
    private String code;
    private String name;
    private Integer termValue;
    private TermTemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
