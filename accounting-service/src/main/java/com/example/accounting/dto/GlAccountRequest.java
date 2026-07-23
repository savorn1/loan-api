package com.example.accounting.dto;

import com.example.accounting.entity.AccountType;
import com.example.accounting.entity.EntrySide;
import com.example.accounting.entity.GlAccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GlAccountRequest {

    private Long parentId;

    @NotBlank
    @Size(max = 20)
    private String accountNo;

    @NotBlank
    @Size(max = 150)
    private String accountName;

    @NotNull
    private AccountType accountType;

    @NotNull
    private EntrySide normalBalance;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private Boolean allowPosting;

    @NotNull
    private GlAccountStatus status;
}
