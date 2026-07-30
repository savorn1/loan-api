package com.example.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Body for POST /api/auth/roles/{roleId}/users — the role id comes from the
// path, so only the user id needs to travel in the request body.
@Data
public class AssignUserToRoleRequest {

    @NotNull
    private Long userId;
}
