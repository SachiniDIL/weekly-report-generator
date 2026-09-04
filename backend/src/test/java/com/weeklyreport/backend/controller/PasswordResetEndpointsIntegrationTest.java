package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.PasswordResetTokenRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import com.weeklyreport.backend.service.BrevoEmailService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, PasswordResetEndpointsIntegrationTest.MeProbeController.class})
class PasswordResetEndpointsIntegrationTest {

    private static final String ORIGINAL_PASSWORD = "original-password";
    private static final String NEW_PASSWORD = "brand-new-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BrevoEmailService brevoEmailService;

    @MockitoBean
    private Clock clock;

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-06-01T12:00:00Z");
        lenient().when(clock.instant()).thenAnswer(invocation -> now);
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void forgotPasswordReturnsTheSameResponseForKnownAndUnknownEmails() throws Exception {
        String unknownResponse = forgotPassword("nobody@example.com");

        persistActiveUser("real@example.com");
        String knownResponse = forgotPassword("real@example.com");

        assertThat(knownResponse).isEqualTo(unknownResponse);
        verify(brevoEmailService, never()).sendPasswordResetEmail(eq("nobody@example.com"), anyString());
        verify(brevoEmailService).sendPasswordResetEmail(eq("real@example.com"), anyString());
        assertThat(tokenRepository.count()).isEqualTo(1);
    }

    @Test
    void aResetTokenCanBeUsedExactlyOnce() throws Exception {
        persistActiveUser("once@example.com");
        forgotPassword("once@example.com");
        String token = tokenFromLink(lastResetLink());

        mockMvc.perform(resetPassword(token, NEW_PASSWORD)).andExpect(status().isOk());
        mockMvc.perform(resetPassword(token, "yet-another-password")).andExpect(status().isBadRequest());

        mockMvc.perform(login("once@example.com", NEW_PASSWORD)).andExpect(status().isOk());
    }

    @Test
    void anExpiredTokenIsRejected() throws Exception {
        persistActiveUser("expired@example.com");
        forgotPassword("expired@example.com");
        String token = tokenFromLink(lastResetLink());

        now = now.plus(Duration.ofMinutes(31));

        mockMvc.perform(resetPassword(token, NEW_PASSWORD)).andExpect(status().isBadRequest());
    }

    @Test
    void requestingASecondResetInvalidatesTheFirstToken() throws Exception {
        persistActiveUser("twice@example.com");

        forgotPassword("twice@example.com");
        String firstToken = tokenFromLink(resetLinks().get(0));

        now = now.plus(Duration.ofMinutes(3));
        forgotPassword("twice@example.com");
        String secondToken = tokenFromLink(resetLinks().get(1));

        assertThat(secondToken).isNotEqualTo(firstToken);
        mockMvc.perform(resetPassword(firstToken, NEW_PASSWORD)).andExpect(status().isBadRequest());
        mockMvc.perform(resetPassword(secondToken, NEW_PASSWORD)).andExpect(status().isOk());
    }

    @Test
    void aTokenIssuedBeforeAResetNoLongerAuthenticatesAfterward() throws Exception {
        User user = persistActiveUser("session@example.com");
        String jwtBeforeReset = jwtService.generateToken(user);

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + jwtBeforeReset))
                .andExpect(status().isOk());

        forgotPassword("session@example.com");
        mockMvc.perform(resetPassword(tokenFromLink(lastResetLink()), NEW_PASSWORD))
                .andExpect(status().isOk());

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + jwtBeforeReset))
                .andExpect(status().isForbidden());
    }

    @Test
    void repeatedForgotPasswordWithinTheCooldownDoesNotIssueASecondToken() throws Exception {
        persistActiveUser("rapid@example.com");

        forgotPassword("rapid@example.com");
        forgotPassword("rapid@example.com");
        forgotPassword("rapid@example.com");

        assertThat(tokenRepository.count()).isEqualTo(1);
        verify(brevoEmailService, times(1)).sendPasswordResetEmail(eq("rapid@example.com"), anyString());
    }

    private User persistActiveUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(ORIGINAL_PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.MEMBER);
        return userRepository.save(user);
    }

    private String forgotPassword(String email) throws Exception {
        return mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private static MockHttpServletRequestBuilder resetPassword(
            String token, String newPassword) {
        return post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token": "%s", "newPassword": "%s"}
                        """.formatted(token, newPassword));
    }

    private static MockHttpServletRequestBuilder login(
            String email, String password) {
        return post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password));
    }

    private List<String> resetLinks() {
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(brevoEmailService, atLeastOnce()).sendPasswordResetEmail(any(), linkCaptor.capture());
        return linkCaptor.getAllValues();
    }

    private String lastResetLink() {
        List<String> links = resetLinks();
        return links.get(links.size() - 1);
    }

    private static String tokenFromLink(String link) {
        return UriComponentsBuilder.fromUriString(link).build().getQueryParams().getFirst("token");
    }

    @RestController
    static class MeProbeController {

        @GetMapping("/me")
        Long me(@AuthenticationPrincipal User user) {
            return user.getId();
        }
    }
}
