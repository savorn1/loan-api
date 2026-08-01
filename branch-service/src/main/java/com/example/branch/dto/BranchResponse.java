package com.example.branch.dto;

import com.example.branch.entity.BranchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private BranchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
