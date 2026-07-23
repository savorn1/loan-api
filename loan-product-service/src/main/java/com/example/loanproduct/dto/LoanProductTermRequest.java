package com.example.loanproduct.dto;

import com.example.loanproduct.entity.TermTemplateStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class LoanProductTermRequest {

    @NotNull
    private UUID termTemplateId;

    @NotNull
    private Boolean isDefault;

    @NotNull
    private TermTemplateStatus status;
}
