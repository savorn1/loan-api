package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.RbacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Management of RBAC roles and their permission/user assignments, gated by the
// ROLE_* permissions (additive layer alongside the existing single Role enum on
// SysUser). hasRole('ADMIN') is kept as an OR fallback so the base admin tier
// never gets locked out by a missing/expired Redis session or a user who was
// never assigned an RBAC role.
@RestController
@RequestMapping("/api/auth/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RbacService rbacService;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.listRoles()));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getRole(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created", rbacService.createRole(request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role updated", rbacService.updateRole(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rbacService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_READ')")
    @GetMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> listPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rbacService.listRolePermissions(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_UPDATE')")
    @PostMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> assignPermission(
            @PathVariable Long id, @Valid @RequestBody AssignPermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission assigned", rbacService.assignPermissionToRole(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_UPDATE')")
    @DeleteMapping("/{id}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<Void>> removePermission(@PathVariable Long id, @PathVariable Long permissionId) {
        rbacService.removePermissionFromRole(id, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed", null));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_READ')")
    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> listUsers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rbacService.listRoleUsers(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_UPDATE')")
    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<UserRoleResponse>> assignUser(
            @PathVariable Long id, @Valid @RequestBody AssignUserToRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "User assigned", rbacService.assignRoleToUser(request.getUserId(), id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_UPDATE')")
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeUser(@PathVariable Long id, @PathVariable Long userId) {
        rbacService.removeRoleFromUser(userId, id);
        return ResponseEntity.ok(ApiResponse.success("User removed", null));
    }
}
