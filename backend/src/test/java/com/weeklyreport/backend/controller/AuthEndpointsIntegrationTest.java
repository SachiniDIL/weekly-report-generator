package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, AuthEndpointsIntegrationTest.ProtectedProbeController.class})
class AuthEndpointsIntegrationTest {

    private static final String PASSWORD = "correct-horse-battery";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registrationCreatesAPendingUserWithNoRole() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("Nadia Reyes", "nadia@example.com", "s3cure-pass")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("pending")));

        User saved = userRepository.findByEmail("nadia@example.com").orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(saved.getRole()).isNull();
        assertThat(saved.getName()).isEqualTo("Nadia Reyes");
        assertThat(passwordEncoder.matches("s3cure-pass", saved.getPasswordHash())).isTrue();
    }

    @Test
    void registrationWithADuplicateEmailIsRejected() throws Exception {
        persistUser("taken@example.com", UserStatus.PENDING, null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("Someone Else", "taken@example.com", "another-pass")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void registrationWithAShortPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("Short Pass", "short@example.com", "abc123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").isNotEmpty());

        assertThat(userRepository.findByEmail("short@example.com")).isEmpty();
    }

    @Test
    void loginWithAPendingAccountReturnsTheSpecificPendingMessage() throws Exception {
        persistUser("pending@example.com", UserStatus.PENDING, null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("pending@example.com", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Your account is pending admin approval"));
    }

    @Test
    void loginWithAWrongPasswordIsRejectedGenerically() throws Exception {
        persistUser("active@example.com", UserStatus.ACTIVE, Role.MEMBER);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("active@example.com", "not-the-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginWithValidActiveCredentialsReturnsATokenTheAuthFilterAccepts() throws Exception {
        User user = persistUser("member@example.com", UserStatus.ACTIVE, Role.MEMBER);

        String responseBody = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("member@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId().intValue()))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.role").value("MEMBER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(responseBody, "$.token");

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(user.getId())));
    }

    private User persistUser(String email, UserStatus status, Role role) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(status);
        user.setRole(role);
        return userRepository.save(user);
    }

    private static String registerBody(String name, String email, String password) {
        return """
                {"name": "%s", "email": "%s", "password": "%s"}
                """.formatted(name, email, password);
    }

    private static String loginBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    @RestController
    static class ProtectedProbeController {

        @GetMapping("/me")
        Long me(@AuthenticationPrincipal User user) {
            return user.getId();
        }
    }
}
