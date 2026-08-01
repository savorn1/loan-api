package com.example.auth.dto;

import lombok.Data;

@Data
public class UpdateBranchRequest {

    // Nullable — unassigns the user's branch when omitted.
    private Long branchId;
}
