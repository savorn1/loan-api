package com.example.loan.dto;

import com.example.loan.entity.GroupLoanApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GroupLoanApplicationResponse {

    private Long id;
    private Long groupId;
    private String groupName;
    private Long branchId;
    private String purpose;
    private GroupLoanApplicationStatus status;
    private List<GroupLoanMemberLineResponse> members;
    private List<GroupLoanApplicationDocumentResponse> documents;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
