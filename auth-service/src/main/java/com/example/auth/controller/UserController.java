package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.AssignUserRoleRequest;
import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.UpdateBranchRequest;
import com.example.auth.dto.UpdateRoleRequest;
import com.example.auth.dto.UpdateStatusRequest;
import com.example.auth.dto.UserFilterRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.dto.UserRoleResponse;
import com.example.auth.exception.AppException;
import com.example.auth.service.RbacService;
import com.example.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// User management, gated by the USER_* permissions. Kept separate from
// AuthController (register/login/refresh/logout/change-password), which every
// authenticated user calls for themselves. hasRole('ADMIN') is kept as an OR
// fallback so the base admin tier never gets locked out by a missing/expired
// Redis session or a user who was never assigned an RBAC role.
@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RbacService rbacService;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> list(@ModelAttribute UserFilterRequest filter) {
        return ResponseEntity.ok(userService.listUsers(filter));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    @GetMapping("/by-ids")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByIds(ids)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", userService.createUser(request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_DELETE')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAll(@RequestBody List<Long> ids, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.deleteUsers(ids, requireUsername(authentication))));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateRoleRequest request,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Role updated",
                userService.updateRole(id, request, requireUsername(authentication))));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateStatusRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                userService.updateStatus(id, request, requireUsername(authentication))));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}/branch")
    public ResponseEntity<ApiResponse<UserResponse>> updateBranch(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", userService.updateBranch(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, requireUsername(authentication));
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @PostMapping("/{id}/force-logout")
    public ResponseEntity<ApiResponse<Void>> forceLogout(@PathVariable Long id) {
        userService.forceLogout(id);
        return ResponseEntity.ok(ApiResponse.success("User logged out", null));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @PostMapping("/force-logout-all")
    public ResponseEntity<ApiResponse<Void>> forceLogoutAll() {
        userService.forceLogoutAll();
        return ResponseEntity.ok(ApiResponse.success("All users logged out", null));
    }

    // Additive RBAC roles held by this user, in addition to their base USER/ADMIN
    // tier (the `role` field/UpdateRoleRequest above, which stays untouched).
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> listRoles(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rbacService.listUserRoles(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserRoleResponse>> assignRole(
            @PathVariable Long id, @Valid @RequestBody AssignUserRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role assigned", rbacService.assignRoleToUser(id, request.getRoleId())));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> removeRole(@PathVariable Long id, @PathVariable Long roleId) {
        rbacService.removeRoleFromUser(id, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
