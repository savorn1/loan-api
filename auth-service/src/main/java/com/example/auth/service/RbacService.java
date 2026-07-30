package com.example.auth.service;

import com.example.auth.dto.*;

import java.util.List;

public interface RbacService {

    List<RoleResponse> listRoles();

    RoleResponse getRole(Long id);

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    void deleteRole(Long id);

    List<PermissionResponse> listPermissions();

    PermissionResponse getPermission(Long id);

    PermissionResponse createPermission(PermissionRequest request);

    PermissionResponse updatePermission(Long id, PermissionRequest request);

    void deletePermission(Long id);

    List<RolePermissionResponse> listRolePermissions(Long roleId);

    RolePermissionResponse assignPermissionToRole(Long roleId, AssignPermissionRequest request);

    void removePermissionFromRole(Long roleId, Long permissionId);

    List<UserRoleResponse> listRoleUsers(Long roleId);

    List<UserRoleResponse> listUserRoles(Long userId);

    UserRoleResponse assignRoleToUser(Long userId, Long roleId);

    void removeRoleFromUser(Long userId, Long roleId);
}
