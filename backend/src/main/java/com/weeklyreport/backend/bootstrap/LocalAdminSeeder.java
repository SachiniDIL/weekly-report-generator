package com.weeklyreport.backend.bootstrap;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds one fixed admin account for local development so there is always a known login.
 * Guarded by the {@code local} profile, so it never runs in production, tests, or CI.
 */
@Component
@Profile("local")
public class LocalAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalAdminSeeder.class);

    private static final String ADMIN_EMAIL = "admin@local.dev";
    private static final String ADMIN_PASSWORD = "localadmin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalAdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setName("Local Admin");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Seeded local development admin {}", ADMIN_EMAIL);
    }
}
