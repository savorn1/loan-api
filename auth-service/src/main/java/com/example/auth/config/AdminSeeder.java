package com.example.auth.config;

import com.example.auth.entity.Role;
import com.example.auth.entity.SysUser;
import com.example.auth.entity.UserStatus;
import com.example.auth.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// New registrations are always created with Role.USER (see AuthServiceImpl), so
// without this there is no way to reach an ADMIN account except a manual DB update.
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // must run before RbacSeeder, which attaches the RBAC ADMIN role to this user
public class AdminSeeder implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.enabled:true}")
    private boolean enabled;

    @Value("${seed.admin.username:admin}")
    private String adminUsername;

    @Value("${seed.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.existsByUsername(adminUsername)) {
            return;
        }
        userRepository.save(SysUser.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
        log.info("Seeded default admin user '{}' (change this password before deploying anywhere real)", adminUsername);
    }
}
