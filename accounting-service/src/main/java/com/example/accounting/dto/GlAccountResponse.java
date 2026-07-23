package com.example.accounting.dto;

import com.example.accounting.entity.AccountType;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.GlAccountStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GlAccountResponse {

    private Long id;
    private Long parentId;
    private String accountNo;
    private String accountName;
    private AccountType accountType;
    private EntrySide normalBalance;
    private String currency;
    private boolean allowPosting;
    private GlAccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
