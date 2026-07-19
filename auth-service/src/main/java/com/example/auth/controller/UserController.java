package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PageResponse;
import com.example.auth.dto.UpdateRoleRequest;
import com.example.auth.dto.UpdateStatusRequest;
import com.example.auth.dto.UserFilterRequest;
import com.example.auth.dto.UserResponse;
import com.example.auth.exception.AppException;
import com.example.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin-only user management: list/create accounts and manage their role/active status.
// Kept separate from AuthController (register/login/refresh/logout/change-password),
// which every authenticated user calls for themselves.
@RestController
@RequestMapping("/api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> list(@ModelAttribute UserFilterRequest filter) {
        return ResponseEntity.ok(userService.listUsers(filter));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-ids")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByIds(ids)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", userService.createUser(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAll(@RequestBody List<Long> ids, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(userService.deleteUsers(ids, requireUsername(authentication))));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateRoleRequest request,
                                                                  Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Role updated",
                userService.updateRole(id, request, requireUsername(authentication))));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateStatusRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                userService.updateStatus(id, request, requireUsername(authentication))));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication authentication) {
        userService.deleteUser(id, requireUsername(authentication));
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/force-logout")
    public ResponseEntity<ApiResponse<Void>> forceLogout(@PathVariable Long id) {
        userService.forceLogout(id);
        return ResponseEntity.ok(ApiResponse.success("User logged out", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/force-logout-all")
    public ResponseEntity<ApiResponse<Void>> forceLogoutAll() {
        userService.forceLogoutAll();
        return ResponseEntity.ok(ApiResponse.success("All users logged out", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
