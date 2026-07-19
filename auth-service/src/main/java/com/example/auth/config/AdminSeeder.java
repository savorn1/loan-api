package com.example.auth.config;

import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// New registrations are always created with Role.USER (see AuthServiceImpl), so
// without this there is no way to reach an ADMIN account except a manual DB update.
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
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
        userRepository.save(User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .active(true)
                .build());
        log.info("Seeded default admin user '{}' (change this password before deploying anywhere real)", adminUsername);
    }
}
