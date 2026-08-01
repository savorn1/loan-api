package com.example.auth.dto;

import lombok.Data;

// Local mirror of branch-service's dto.BranchResponse — only the fields auth-service
// actually needs (display name) for enriching UserResponse.branchName.
@Data
public class BranchResponse {

    private Long id;
    private String code;
    private String name;
}
