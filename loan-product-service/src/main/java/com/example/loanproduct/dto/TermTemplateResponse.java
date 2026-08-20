package com.example.loanproduct.dto;

import com.example.loanproduct.entity.TermTemplateStatus;
import com.example.loanproduct.entity.TermUnit;
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
    private TermUnit termUnit;
    private TermTemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
