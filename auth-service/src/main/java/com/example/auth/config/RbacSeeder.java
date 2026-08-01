package com.example.auth.config;

import com.example.auth.entity.Permission;
import com.example.auth.entity.RbacRole;
import com.example.auth.entity.SysUser;
import com.example.auth.repository.PermissionRepository;
import com.example.auth.repository.RbacRoleRepository;
import com.example.auth.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Seeds a starter set of CRUD permissions covering every domain resource in the system
// (not just what auth-service itself manages), plus two roles built from them: ADMIN
// (every permission) and VIEWER (read-only). Nothing in the platform enforces these
// permissions yet (see the comment on Permission) — this just gives the RBAC CRUD
// endpoints (RoleController/PermissionController) non-empty reference data, and attaches
// the seeded admin user (see AdminSeeder) to the ADMIN role so it isn't orphaned data.
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class RbacSeeder implements CommandLineRunner {

    private static final Map<String, String> RESOURCES = new LinkedHashMap<>();
    static {
        RESOURCES.put("USER", "users");
        RESOURCES.put("ROLE", "roles");
        RESOURCES.put("PERMISSION", "permissions");
        RESOURCES.put("CUSTOMER", "customers");
        RESOURCES.put("LOAN", "loans");
        RESOURCES.put("PAYMENT", "payments");
        RESOURCES.put("LOAN_PRODUCT", "loan products");
        RESOURCES.put("ACCOUNTING", "accounting records");
        RESOURCES.put("BRANCH", "branches");
    }

    private static final Map<String, String> ACTIONS = new LinkedHashMap<>();
    static {
        ACTIONS.put("READ", "View");
        ACTIONS.put("CREATE", "Create");
        ACTIONS.put("UPDATE", "Update");
        ACTIONS.put("DELETE", "Delete");
    }

    private final PermissionRepository permissionRepository;
    private final RbacRoleRepository roleRepository;
    private final SysUserRepository userRepository;

    @Value("${seed.rbac.enabled:true}")
    private boolean enabled;

    @Value("${seed.admin.username:admin}")
    private String adminUsername;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        List<Permission> permissions = RESOURCES.entrySet().stream()
                .flatMap(resource -> ACTIONS.entrySet().stream()
                        .map(action -> seedPermission(resource.getKey(), resource.getValue(),
                                action.getKey(), action.getValue())))
                .toList();

        RbacRole adminRole = seedRole("ADMIN", "Full access to every seeded permission");
        RbacRole viewerRole = seedRole("VIEWER", "Read-only access across the system");

        permissions.forEach(permission -> assignPermission(adminRole, permission));
        permissions.stream()
                .filter(permission -> "READ".equals(permission.getAction()))
                .forEach(permission -> assignPermission(viewerRole, permission));

        userRepository.findByUsername(adminUsername).ifPresent(admin -> assignRole(admin, adminRole));

        log.info("Seeded {} permissions and 2 roles (ADMIN, VIEWER)", permissions.size());
    }

    private Permission seedPermission(String resourceCode, String resourceLabel, String actionCode, String actionLabel) {
        return permissionRepository.findByModuleAndAction(resourceCode, actionCode)
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .module(resourceCode)
                        .action(actionCode)
                        .description(actionLabel + " " + resourceLabel)
                        .build()));
    }

    private RbacRole seedRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(RbacRole.builder()
                        .name(name)
                        .code(name)
                        .description(description)
                        .build()));
    }

    private void assignPermission(RbacRole role, Permission permission) {
        if (role.getPermissions().contains(permission)) {
            return;
        }
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    private void assignRole(SysUser user, RbacRole role) {
        if (user.getRoles().contains(role)) {
            return;
        }
        user.getRoles().add(role);
        userRepository.save(user);
    }
}
