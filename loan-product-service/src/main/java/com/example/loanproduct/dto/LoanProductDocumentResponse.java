package com.example.loanproduct.dto;

import com.example.loanproduct.entity.DocumentTemplateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductDocumentResponse {

    private Long id;
    private UUID loanProductId;
    private UUID documentTemplateId;
    private String documentTemplateCode;
    private String documentTemplateName;
    private String documentTemplateDescription;
    private Boolean required;
    private DocumentTemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
