package com.example.loan.dto;

import com.example.loan.entity.LoanDocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanDocumentStatusUpdateRequest {

    @NotNull
    private LoanDocumentStatus status;
}
