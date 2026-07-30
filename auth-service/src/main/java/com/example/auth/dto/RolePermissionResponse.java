package com.example.auth.dto;

import lombok.Builder;
import lombok.Data;

// No `id`/`createdAt` here — role↔permission assignment is a plain @ManyToMany
// join table (role_permission: role_id, permission_id only), not a tracked row.
@Data
@Builder
public class RolePermissionResponse {

    private Long roleId;
    private Long permissionId;
    private String permissionName;
    private String permissionModule;
    private String permissionAction;
    private String permissionDescription;
}
