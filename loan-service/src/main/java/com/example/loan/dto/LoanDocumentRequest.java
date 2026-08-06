package com.example.loan.dto;

import com.example.loan.entity.LoanDocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanDocumentRequest {

    @NotBlank
    private String name;

    @NotNull
    private LoanDocumentStatus status;

    private String notes;
}
