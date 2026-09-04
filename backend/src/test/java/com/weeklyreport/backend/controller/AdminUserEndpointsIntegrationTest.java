package com.weeklyreport.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.security.JwtService;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, AdminUserEndpointsIntegrationTest.MeProbeController.class})
class AdminUserEndpointsIntegrationTest {

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

    static Stream<Arguments> nonAdminRoleAndEndpoint() {
        return List.of(Role.MANAGER, Role.MEMBER).stream()
                .flatMap(role -> adminEndpoints().map(endpoint -> Arguments.of(role, endpoint)));
    }

    private static Stream<Named<MockHttpServletRequestBuilder>> adminEndpoints() {
        return Stream.of(
                Named.of("GET /admin/users", get("/admin/users")),
                Named.of(
                        "POST /admin/users",
                        post("/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody("X", "x@example.com", "password12", "MEMBER"))),
                Named.of(
                        "POST /admin/users/1/approve",
                        post("/admin/users/1/approve")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(roleBody("MEMBER"))),
                Named.of("DELETE /admin/users/1", delete("/admin/users/1")),
                Named.of(
                        "PATCH /admin/users/1/role",
                        patch("/admin/users/1/role")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(roleBody("MEMBER"))));
    }

    @ParameterizedTest(name = "{0} is forbidden from {1}")
    @MethodSource("nonAdminRoleAndEndpoint")
    void nonAdminRolesAreForbiddenFromEveryAdminEndpoint(
            Role role, MockHttpServletRequestBuilder request) throws Exception {
        User user = persistUser("Non Admin", "nonadmin@example.com", UserStatus.ACTIVE, role);

        mockMvc.perform(request.header("Authorization", bearer(user)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "an unauthenticated request is rejected from {0}")
    @MethodSource("adminEndpoints")
    void unauthenticatedRequestsAreRejectedFromEveryAdminEndpoint(
            MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request).andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    void adminCreatesAnImmediatelyActiveUserWithTheGivenRole() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);

        mockMvc.perform(post("/admin/users")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Priya Nair", "priya@example.com", "password12", "MANAGER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("MANAGER"));

        User created = userRepository.findByEmail("priya@example.com").orElseThrow();
        assertThat(created.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(created.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void approvingAPendingUserLetsThemLogInWithTheAssignedRole() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);
        User pending = persistUser("Pending Person", "pending@example.com", UserStatus.PENDING, null);

        mockMvc.perform(post("/admin/users/" + pending.getId() + "/approve")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("MANAGER"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("pending@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("MANAGER"));
    }

    @Test
    void approvingANonPendingUserIsRejected() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);
        User active = persistUser("Already Active", "active@example.com", UserStatus.ACTIVE, Role.MEMBER);

        mockMvc.perform(post("/admin/users/" + active.getId() + "/approve")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleBody("MANAGER")))
                .andExpect(status().isConflict());
    }

    @Test
    void removingAnActiveUserSoftDeletesTheRowAndInvalidatesExistingTokens() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);
        User member = persistUser("Member", "member@example.com", UserStatus.ACTIVE, Role.MEMBER);
        String memberToken = jwtService.generateToken(member);

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/admin/users/" + member.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        User reloaded = userRepository.findById(member.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(UserStatus.REMOVED);
        assertThat(reloaded.getTokenVersion()).isEqualTo(1);

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void removingAPendingUserDeletesTheRowOutright() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);
        User pending = persistUser("Rejected Signup", "rejected@example.com", UserStatus.PENDING, null);

        mockMvc.perform(delete("/admin/users/" + pending.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(pending.getId())).isEmpty();
    }

    @Test
    void aRejectedPendingUsersEmailCanBeRegisteredAgain() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);
        User pending = persistUser("Rejected Signup", "reuse@example.com", UserStatus.PENDING, null);

        mockMvc.perform(delete("/admin/users/" + pending.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicRegisterBody("Fresh Signup", "reuse@example.com", "password12")))
                .andExpect(status().isCreated());

        User reRegistered = userRepository.findByEmail("reuse@example.com").orElseThrow();
        assertThat(reRegistered.getId()).isNotEqualTo(pending.getId());
        assertThat(reRegistered.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    void aSoftDeletedActiveUsersEmailStaysBlockedFromReRegistration() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);
        User active = persistUser("Departed", "kept@example.com", UserStatus.ACTIVE, Role.MEMBER);

        mockMvc.perform(delete("/admin/users/" + active.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(userRepository.findByEmail("kept@example.com").orElseThrow().getStatus())
                .isEqualTo(UserStatus.REMOVED);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicRegisterBody("Impostor", "kept@example.com", "password12")))
                .andExpect(status().isConflict());
    }

    @Test
    void operatingOnAnUnknownUserIdReturnsACleanNotFound() throws Exception {
        User admin = persistUser("Admin", "admin@example.com", UserStatus.ACTIVE, Role.ADMIN);

        mockMvc.perform(delete("/admin/users/999999").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
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

    private static String roleBody(String role) {
        return """
                {"role": "%s"}
                """.formatted(role);
    }

    private static String loginBody(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    private static String createBody(String name, String email, String password, String role) {
        return """
                {"name": "%s", "email": "%s", "password": "%s", "role": "%s"}
                """.formatted(name, email, password, role);
    }

    private static String publicRegisterBody(String name, String email, String password) {
        return """
                {"name": "%s", "email": "%s", "password": "%s"}
                """.formatted(name, email, password);
    }

    @RestController
    static class MeProbeController {

        @GetMapping("/me")
        Long me(@AuthenticationPrincipal User user) {
            return user.getId();
        }
    }
}
