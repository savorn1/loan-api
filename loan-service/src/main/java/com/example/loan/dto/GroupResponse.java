package com.example.loan.dto;

import com.example.loan.entity.GroupStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GroupResponse {

    private Long id;
    private String name;
    private String code;
    private UUID loanProductId;
    private String loanProductName;
    private Long branchId;
    private String branchName;
    private Long leaderCustomerId;
    private String leaderName;
    private int memberCount;
    private LocalDate formationDate;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
