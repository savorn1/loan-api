package com.example.loanproduct.dto;

import com.example.loanproduct.entity.DocumentTemplateStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LoanProductDocumentRequest {

    @NotNull
    private UUID documentTemplateId;

    @NotNull
    private Boolean required;

    @NotNull
    private DocumentTemplateStatus status;
}
