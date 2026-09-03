package com.weeklyreport.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-definitely-long-enough-for-hs256";
    private static final String OTHER_SECRET = "a-completely-different-secret-also-long-enough-yes";

    private final JwtService jwtService = new JwtService(SECRET);

    @Test
    void generatedTokenRoundTripsEveryClaim() {
        User user = activeManager();

        Claims claims = jwtService.parseToken(jwtService.generateToken(user));

        assertThat(jwtService.extractUserId(claims)).isEqualTo(42L);
        assertThat(jwtService.extractRole(claims)).isEqualTo(Role.MANAGER);
        assertThat(jwtService.extractStatus(claims)).isEqualTo(UserStatus.ACTIVE);
        assertThat(jwtService.extractTokenVersion(claims)).isEqualTo(7);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void refusesToIssueATokenForAUserWithNoRole() {
        assertThatThrownBy(() -> jwtService.generateToken(rolelessUser()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot issue a token for a user with no role assigned");
    }

    @Test
    void expiredTokenIsRejected() {
        String expired =
                jwtService.generateToken(activeManager(), Instant.now().minus(Duration.ofHours(25)));

        assertThatThrownBy(() -> jwtService.parseToken(expired))
                .isInstanceOf(InvalidTokenException.class)
                .hasCauseInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithADifferentSecretIsRejected() {
        String foreignToken = new JwtService(OTHER_SECRET).generateToken(activeManager());

        assertThatThrownBy(() -> jwtService.parseToken(foreignToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    private User activeManager() {
        User user = new User();
        user.setId(42L);
        user.setEmail("manager@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setRole(Role.MANAGER);
        user.setStatus(UserStatus.ACTIVE);
        user.setTokenVersion(7);
        return user;
    }

    private User rolelessUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("new@example.com");
        user.setPasswordHash("not-a-real-hash");
        user.setStatus(UserStatus.PENDING);
        return user;
    }
}
