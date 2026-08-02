package com.example.loan.dto;

import com.example.loan.entity.GroupMemberRole;
import com.example.loan.entity.GroupMemberStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GroupMemberResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private GroupMemberRole role;
    private GroupMemberStatus status;
    // Server-derived: "VERIFIED" if the customer has >=1 ACTIVE identity on file, else "PENDING".
    private String kycStatus;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
