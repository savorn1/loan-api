package com.example.loan.dto;

import com.example.loan.entity.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationDocumentResponse {

    private Long id;
    private Long applicationId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private DocumentStatus status;
    private LocalDateTime uploadedAt;
}
