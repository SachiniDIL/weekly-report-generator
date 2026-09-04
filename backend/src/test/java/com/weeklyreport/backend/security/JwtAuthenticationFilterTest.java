package com.weeklyreport.backend.security;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.weeklyreport.backend.TestcontainersConfiguration;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, JwtAuthenticationFilterTest.ProbeController.class})
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void permittedPathIsReachableWithoutAToken() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void protectedPathIsRejectedWithoutAToken() throws Exception {
        mockMvc.perform(get("/whoami")).andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    void tokenWithStaleTokenVersionIsNotAuthenticated() throws Exception {
        User user = persistActiveMember(0);
        String token = jwtService.generateToken(user);

        user.setTokenVersion(1);
        userRepository.save(user);

        mockMvc.perform(get("/whoami").header("Authorization", "Bearer " + token))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    void currentTokenAuthenticatesAndExposesTheUserEntityAsPrincipal() throws Exception {
        User user = persistActiveMember(2);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/whoami").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(user.getId())));
    }

    private User persistActiveMember(int tokenVersion) {
        User user = new User();
        user.setName("Probe User");
        user.setEmail("probe@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setRole(Role.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        user.setTokenVersion(tokenVersion);
        return userRepository.save(user);
    }

    @RestController
    static class ProbeController {

        @GetMapping("/auth/ping")
        String ping() {
            return "ok";
        }

        @GetMapping("/whoami")
        Long whoami(@AuthenticationPrincipal User user) {
            return user.getId();
        }
    }
}
