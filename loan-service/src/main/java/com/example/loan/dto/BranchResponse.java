package com.example.loan.dto;

import lombok.Data;

// Local mirror of branch-service's dto.BranchResponse — only the fields this
// service needs (display name) for enriching Group response DTOs.
@Data
public class BranchResponse {

    private Long id;
    private String code;
    private String name;
}
