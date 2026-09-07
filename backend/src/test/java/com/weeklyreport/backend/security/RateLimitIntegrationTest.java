package com.weeklyreport.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.service.BrevoEmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * MockMvc has no real remote address, so every request carries a fixed {@code X-Forwarded-For}
 * to give the filter a stable IP to bucket against. The limiter is off in the shared test
 * profile; this class re-enables it for its own (isolated) context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "rate-limit.enabled=true")
class RateLimitIntegrationTest {

    private static final String CLIENT_IP = "198.51.100.23";
    private static final String PASSWORD = "correct-horse-battery";
    private static final String TOO_MANY_REQUESTS_MESSAGE =
            "Too many requests. Please wait a moment before trying again.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BrevoEmailService brevoEmailService;

    @BeforeEach
    @AfterEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void theEleventhLoginAttemptFromOneIpIsRejectedWithAConsistent429Body() throws Exception {
        for (int attempt = 1; attempt <= 10; attempt++) {
            mockMvc.perform(loginFromClientIp("wrong")).andExpect(status().isUnauthorized());
        }

        mockMvc.perform(loginFromClientIp("wrong"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(TOO_MANY_REQUESTS_MESSAGE))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void theSixthRegisterAttemptFromOneIpIsRejectedWith429() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(registerFromClientIp("user" + attempt + "@example.com"))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(registerFromClientIp("user6@example.com"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(jsonPath("$.message").value(TOO_MANY_REQUESTS_MESSAGE));
    }

    @Test
    void theSixthForgotPasswordAttemptFromOneIpIsRejectedWith429() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(forgotPasswordFromClientIp("someone" + attempt + "@example.com"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(forgotPasswordFromClientIp("someone6@example.com"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void anAuthenticatedEndpointIsNeverRateLimited() throws Exception {
        String bearer = "Bearer " + jwtService.generateToken(persistManager());

        for (int i = 0; i < 25; i++) {
            mockMvc.perform(fromClientIp(get("/users").header(HttpHeaders.AUTHORIZATION, bearer)))
                    .andExpect(status().isOk());
        }
    }

    private MockHttpServletRequestBuilder loginFromClientIp(String password) {
        return fromClientIp(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"nobody@example.com\", \"password\": \"%s\"}".formatted(password)));
    }

    private MockHttpServletRequestBuilder registerFromClientIp(String email) {
        return fromClientIp(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Person\", \"email\": \"%s\", \"password\": \"password1\"}"
                        .formatted(email)));
    }

    private MockHttpServletRequestBuilder forgotPasswordFromClientIp(String email) {
        return fromClientIp(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"%s\"}".formatted(email)));
    }

    private static MockHttpServletRequestBuilder fromClientIp(MockHttpServletRequestBuilder builder) {
        return builder.header("X-Forwarded-For", CLIENT_IP);
    }

    private User persistManager() {
        User user = new User();
        user.setName("Rate Limit Manager");
        user.setEmail("rl-manager@example.com");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.MANAGER);
        return userRepository.save(user);
    }
}
