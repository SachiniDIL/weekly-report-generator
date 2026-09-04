package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import java.time.Instant;

/** Fuller user projection for the admin user list — more than login's {@code UserSummary}. */
public record AdminUserView(
        Long id, String name, String email, Role role, UserStatus status, Instant createdAt) {

    public static AdminUserView from(User user) {
        return new AdminUserView(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }
}
