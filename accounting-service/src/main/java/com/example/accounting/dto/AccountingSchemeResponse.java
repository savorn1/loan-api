package com.example.accounting.dto;

import com.example.accounting.entity.AccountingSchemeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountingSchemeResponse {

    private Long id;
    private Long journalTemplateId;
    private String accountRole;
    private Long glAccountId;
    private String glAccountNo;
    private String currency;
    private AccountingSchemeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
