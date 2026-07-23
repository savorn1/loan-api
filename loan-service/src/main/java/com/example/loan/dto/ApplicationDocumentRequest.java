package com.example.loan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationDocumentRequest {

    @NotBlank
    private String documentType;

    @NotBlank
    private String fileName;

    @NotBlank
    private String fileUrl;
}
