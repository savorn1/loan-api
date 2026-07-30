package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

// No `id`/`createdAt` here — user↔role assignment is a plain @ManyToMany join
// table (user_role: user_id, role_id only), not a tracked row.
@Data
@Builder
public class UserRoleResponse {

    private Long userId;
    private String username;
    private Long roleId;
    private String roleName;
}
