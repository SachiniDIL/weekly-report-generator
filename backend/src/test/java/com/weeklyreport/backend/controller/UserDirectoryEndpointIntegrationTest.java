package com.weeklyreport.backend.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserDirectoryEndpointIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void aMemberIsForbiddenFromTheUserDirectory() throws Exception {
        User member = persistUser("Member", "member@example.com", UserStatus.ACTIVE, Role.MEMBER);

        mockMvc.perform(get("/users").header("Authorization", bearer(member)))
                .andExpect(status().isForbidden());
    }

    @Test
    void aManagerAndAnAdminCanBothListUsers() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", UserStatus.ACTIVE, Role.MANAGER);
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);

        mockMvc.perform(get("/users").header("Authorization", bearer(manager)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void listsOnlyActiveUsersAndHonoursTheRoleFilter() throws Exception {
        User manager = persistUser("Manager", "manager@example.com", UserStatus.ACTIVE, Role.MANAGER);
        persistUser("Active Member", "active@example.com", UserStatus.ACTIVE, Role.MEMBER);
        persistUser("Pending Member", "pending@example.com", UserStatus.PENDING, null);
        persistUser("Removed Member", "removed@example.com", UserStatus.REMOVED, Role.MEMBER);

        mockMvc.perform(get("/users").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Manager", "Active Member")));

        mockMvc.perform(get("/users").param("role", "MEMBER").header("Authorization", bearer(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Active Member"))
                .andExpect(jsonPath("$[0].role").value("MEMBER"));
    }

    private User persistUser(String name, String email, UserStatus status, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(status);
        user.setRole(role);
        return userRepository.save(user);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
