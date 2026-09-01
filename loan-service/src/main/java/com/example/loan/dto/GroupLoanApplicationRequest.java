package com.example.loan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GroupLoanApplicationRequest {

    @NotNull
    private Long groupId;

    // Optional — the staff member (auth-service user) who originated this
    // application. Same unvalidated cross-service-id convention as groupId.
    private Long loanOfficerId;

    private String purpose;

    @NotEmpty
    @Valid
    private List<GroupLoanMemberRequest> members;
}
