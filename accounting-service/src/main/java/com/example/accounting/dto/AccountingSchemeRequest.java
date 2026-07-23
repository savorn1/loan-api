package com.example.accounting.dto;

import com.example.accounting.entity.AccountingSchemeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountingSchemeRequest {

    @NotNull
    private Long journalTemplateId;

    @NotBlank
    @Size(max = 50)
    private String accountRole;

    @NotNull
    private Long glAccountId;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private AccountingSchemeStatus status;
}
