package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.PasswordResetToken;
import com.weeklyreport.backend.domain.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
            String tokenHash, Instant now);

    Optional<PasswordResetToken> findFirstByUserOrderByCreatedAtDesc(User user);

    long deleteByUserAndUsedAtIsNullAndExpiresAtAfter(User user, Instant now);
}
