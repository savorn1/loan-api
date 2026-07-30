package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.PermissionRequest;
import com.example.auth.dto.PermissionResponse;
import com.example.auth.service.RbacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Management of RBAC permissions, gated by the PERMISSION_* permissions themselves
// (reference data — nothing else in the platform enforces permissions yet, see
// RbacRole/Permission entity javadoc). hasRole('ADMIN') is kept as an OR fallback
// so the base admin tier never gets locked out by a missing/expired Redis session
// or a user who was never assigned an RBAC role.
@RestController
@RequestMapping("/api/auth/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final RbacService rbacService;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.listPermissions()));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getPermission(id)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created", rbacService.createPermission(request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Permission updated", rbacService.updatePermission(id, request)));
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        rbacService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted", null));
    }
}
