package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Audits the shared error surface across the auth, admin, and password-reset controllers:
 * malformed input must never come back with a stack trace, an exception type, or an
 * internal message.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ErrorHandlingIntegrationTest {

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

    @ParameterizedTest(name = "malformed JSON to {0} returns a clean error")
    @ValueSource(
            strings = {
                "/auth/register",
                "/auth/login",
                "/auth/forgot-password",
                "/auth/reset-password"
            })
    void malformedJsonToPermittedEndpointsReturnsACleanError(String path) throws Exception {
        String body = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json "))
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNoInternalDetail(body);
    }

    @Test
    void malformedJsonToAnAdminEndpointReturnsACleanError() throws Exception {
        User admin = persistAdmin();

        String body = mockMvc.perform(post("/admin/users")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ broken"))
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNoInternalDetail(body);
    }

    @Test
    void missingRequiredFieldsReturnACleanValidationError() throws Exception {
        String body = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNoInternalDetail(body);
    }

    private static void assertNoInternalDetail(String body) {
        assertThat(body)
                .doesNotContainIgnoringCase("exception")
                .doesNotContainIgnoringCase("\"trace\"")
                .doesNotContain("com.weeklyreport")
                .doesNotContain("org.springframework")
                .doesNotContain("\tat ");
    }

    private User persistAdmin() {
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("admin-password"));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(Role.ADMIN);
        return userRepository.save(admin);
    }
}
