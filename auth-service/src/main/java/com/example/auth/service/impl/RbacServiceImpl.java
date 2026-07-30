package com.example.auth.service.impl;

import com.example.auth.dto.*;
import com.example.auth.entity.Permission;
import com.example.auth.entity.RbacRole;
import com.example.auth.entity.SysUser;
import com.example.auth.exception.AppException;
import com.example.auth.repository.PermissionRepository;
import com.example.auth.repository.RbacRoleRepository;
import com.example.auth.repository.SysUserRepository;
import com.example.auth.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final RbacRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SysUserRepository userRepository;

    @Override
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(this::toRoleResponse).toList();
    }

    @Override
    public RoleResponse getRole(Long id) {
        return toRoleResponse(findRole(id));
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Role name already taken: " + request.getName());
        }
        if (roleRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Role code already taken: " + request.getCode());
        }
        RbacRole role = RbacRole.builder()
                .name(request.getName())
                .code(request.getCode())
                .isDefault(request.isDefault())
                .description(request.getDescription())
                .build();
        return toRoleResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        RbacRole role = findRole(id);
        if (!role.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            throw new AppException(HttpStatus.CONFLICT, "Role name already taken: " + request.getName());
        }
        if (!role.getCode().equals(request.getCode()) && roleRepository.existsByCode(request.getCode())) {
            throw new AppException(HttpStatus.CONFLICT, "Role code already taken: " + request.getCode());
        }
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDefault(request.isDefault());
        role.setDescription(request.getDescription());
        return toRoleResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        RbacRole role = findRole(id);
        role.getPermissions().clear();
        roleRepository.save(role);
        new HashSet<>(role.getUsers()).forEach(user -> {
            user.getRoles().remove(role);
            userRepository.save(user);
        });
        roleRepository.delete(role);
    }

    @Override
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream().map(this::toPermissionResponse).toList();
    }

    @Override
    public PermissionResponse getPermission(Long id) {
        return toPermissionResponse(findPermission(id));
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.existsByModuleAndAction(request.getModule(), request.getAction())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Permission already exists for module " + request.getModule() + " and action " + request.getAction());
        }
        Permission permission = Permission.builder()
                .module(request.getModule())
                .action(request.getAction())
                .description(request.getDescription())
                .build();
        return toPermissionResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = findPermission(id);
        boolean changingKey = !permission.getModule().equals(request.getModule())
                || !permission.getAction().equals(request.getAction());
        if (changingKey && permissionRepository.existsByModuleAndAction(request.getModule(), request.getAction())) {
            throw new AppException(HttpStatus.CONFLICT,
                    "Permission already exists for module " + request.getModule() + " and action " + request.getAction());
        }
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());
        return toPermissionResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = findPermission(id);
        roleRepository.findByPermissions_Id(id).forEach(role -> {
            role.getPermissions().remove(permission);
            roleRepository.save(role);
        });
        permissionRepository.delete(permission);
    }

    @Override
    public List<RolePermissionResponse> listRolePermissions(Long roleId) {
        RbacRole role = findRole(roleId);
        return role.getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getModule).thenComparing(Permission::getAction))
                .map(permission -> toRolePermissionResponse(role, permission))
                .toList();
    }

    @Override
    @Transactional
    public RolePermissionResponse assignPermissionToRole(Long roleId, AssignPermissionRequest request) {
        RbacRole role = findRole(roleId);
        Permission permission = findPermission(request.getPermissionId());
        role.getPermissions().add(permission);
        roleRepository.save(role);
        return toRolePermissionResponse(role, permission);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        RbacRole role = findRole(roleId);
        role.getPermissions().removeIf(permission -> permission.getId().equals(permissionId));
        roleRepository.save(role);
    }

    @Override
    public List<UserRoleResponse> listRoleUsers(Long roleId) {
        RbacRole role = findRole(roleId);
        return role.getUsers().stream()
                .sorted(Comparator.comparing(SysUser::getUsername))
                .map(user -> toUserRoleResponse(user, role))
                .toList();
    }

    @Override
    public List<UserRoleResponse> listUserRoles(Long userId) {
        SysUser user = findUser(userId);
        return user.getRoles().stream()
                .sorted(Comparator.comparing(RbacRole::getName))
                .map(role -> toUserRoleResponse(user, role))
                .toList();
    }

    @Override
    @Transactional
    public UserRoleResponse assignRoleToUser(Long userId, Long roleId) {
        SysUser user = findUser(userId);
        RbacRole role = findRole(roleId);
        user.getRoles().add(role);
        userRepository.save(user);
        return toUserRoleResponse(user, role);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        SysUser user = findUser(userId);
        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        userRepository.save(user);
    }

    private RbacRole findRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Role not found with id: " + id));
    }

    private Permission findPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Permission not found with id: " + id));
    }

    private SysUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    private RoleResponse toRoleResponse(RbacRole role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .isDefault(role.isDefault())
                .description(role.getDescription())
                .permissionCount(role.getPermissions().size())
                .userCount(role.getUsers().size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .module(permission.getModule())
                .action(permission.getAction())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    private RolePermissionResponse toRolePermissionResponse(RbacRole role, Permission permission) {
        return RolePermissionResponse.builder()
                .roleId(role.getId())
                .permissionId(permission.getId())
                .permissionName(permission.getName())
                .permissionModule(permission.getModule())
                .permissionAction(permission.getAction())
                .permissionDescription(permission.getDescription())
                .build();
    }

    private UserRoleResponse toUserRoleResponse(SysUser user, RbacRole role) {
        return UserRoleResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roleId(role.getId())
                .roleName(role.getName())
                .build();
    }
}
