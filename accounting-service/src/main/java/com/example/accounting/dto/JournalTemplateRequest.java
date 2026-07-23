package com.example.accounting.dto;

import com.example.accounting.entity.JournalTemplateStatus;
import com.example.accounting.entity.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class JournalTemplateRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private TransactionType transactionType;

    private String description;

    @NotNull
    private JournalTemplateStatus status;

    @NotEmpty
    @Valid
    private List<JournalTemplateLineRequest> lines;
}
