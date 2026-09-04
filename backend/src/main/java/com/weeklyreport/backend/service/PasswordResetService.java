package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.PasswordResetToken;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.exception.InvalidPasswordResetTokenException;
import com.weeklyreport.backend.repository.PasswordResetTokenRepository;
import com.weeklyreport.backend.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

/** Issues and consumes single-use password reset tokens. */
@Service
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(2);
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoEmailService emailService;
    private final Clock clock;
    private final String frontendUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            BrevoEmailService emailService,
            Clock clock,
            @Value("${frontend.url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.clock = clock;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> activeUser = userRepository
                .findByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE);
        if (activeUser.isEmpty()) {
            return;
        }
        User user = activeUser.get();

        Instant now = clock.instant();
        if (isWithinResendCooldown(user, now)) {
            return;
        }

        tokenRepository.deleteByUserAndUsedAtIsNullAndExpiresAtAfter(user, now);

        String rawToken = generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(sha256(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(TOKEN_TTL));
        tokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), buildResetLink(rawToken));
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(sha256(rawToken), clock.instant())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        token.setUsedAt(clock.instant());
    }

    private boolean isWithinResendCooldown(User user, Instant now) {
        return tokenRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .map(latest -> latest.getCreatedAt().isAfter(now.minus(RESEND_COOLDOWN)))
                .orElse(false);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildResetLink(String rawToken) {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .toUriString();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
