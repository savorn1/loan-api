package com.example.loanproduct.dto;

import com.example.loanproduct.entity.DocumentTemplateStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentTemplateRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    private DocumentTemplateStatus status;
}
