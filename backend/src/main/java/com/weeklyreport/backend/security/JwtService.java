package com.weeklyreport.backend.security;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates and verifies the HS256-signed access tokens used for authentication.
 * Holds no request state and does no authorization itself.
 */
@Service
public class JwtService {

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_STATUS = "status";
    private static final String CLAIM_TOKEN_VERSION = "tokenVersion";

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        return generateToken(user, Instant.now());
    }

    // Package-private so tests can issue tokens at an arbitrary point in time.
    String generateToken(User user, Instant issuedAt) {
        if (user.getRole() == null) {
            throw new IllegalStateException("Cannot issue a token for a user with no role assigned");
        }
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_STATUS, user.getStatus().name())
                .claim(CLAIM_TOKEN_VERSION, user.getTokenVersion())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(TOKEN_TTL)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        }
    }

    public long extractUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public Role extractRole(Claims claims) {
        return Role.valueOf(claims.get(CLAIM_ROLE, String.class));
    }

    public UserStatus extractStatus(Claims claims) {
        return UserStatus.valueOf(claims.get(CLAIM_STATUS, String.class));
    }

    public int extractTokenVersion(Claims claims) {
        return claims.get(CLAIM_TOKEN_VERSION, Integer.class);
    }
}
