package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
}
