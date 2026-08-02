package com.example.loan.dto;

import com.example.loan.entity.ApprovalDecision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GroupLoanApplicationApprovalRequest {

    @NotNull
    private ApprovalDecision decision;

    // Required, one entry per application member, when decision is APPROVED.
    @Valid
    private List<GroupLoanApprovalMemberDecisionRequest> members;

    private String comments;
}
