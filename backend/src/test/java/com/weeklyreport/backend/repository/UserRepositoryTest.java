package com.weeklyreport.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void persistsAndReloadsNativePostgresEnumColumns() {
        User user = new User();
        user.setName("Ada Lovelace");
        user.setEmail("ada@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setRole(Role.MANAGER);
        user.setStatus(UserStatus.ACTIVE);

        Long savedId = userRepository.save(user).getId();

        User reloaded = userRepository.findByEmail("ada@example.com").orElseThrow();
        assertThat(reloaded.getId()).isEqualTo(savedId);
        assertThat(reloaded.getRole()).isEqualTo(Role.MANAGER);
        assertThat(reloaded.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(reloaded.getTokenVersion()).isZero();
        assertThat(reloaded.getCreatedAt()).isNotNull();
    }

    @Test
    void defaultsStatusToPendingAndLeavesRoleNull() {
        User user = new User();
        user.setName("Grace Hopper");
        user.setEmail("grace@example.com");
        user.setPasswordHash("not-a-real-hash");

        userRepository.save(user);

        User reloaded = userRepository.findByEmail("grace@example.com").orElseThrow();
        assertThat(reloaded.getRole()).isNull();
        assertThat(reloaded.getStatus()).isEqualTo(UserStatus.PENDING);
    }
}
