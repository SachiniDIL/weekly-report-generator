package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.User;

/** A project's assigned member — the {@link AdminUserView} identity fields, nothing more. */
public record ProjectMemberView(Long userId, String name, String email) {

    public static ProjectMemberView from(User user) {
        return new ProjectMemberView(user.getId(), user.getName(), user.getEmail());
    }
}
