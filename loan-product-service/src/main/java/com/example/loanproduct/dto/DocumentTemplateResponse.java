package com.example.loanproduct.dto;

import com.example.loanproduct.entity.DocumentTemplateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DocumentTemplateResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private DocumentTemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
