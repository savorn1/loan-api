package com.example.loanproduct.dto;

import com.example.loanproduct.entity.TermTemplateStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LoanProductTermResponse {

    private Long id;
    private UUID loanProductId;
    private UUID termTemplateId;
    private String termTemplateCode;
    private String termTemplateName;
    private Integer termValue;
    private Boolean isDefault;
    private TermTemplateStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
