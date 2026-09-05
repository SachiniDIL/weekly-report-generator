package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;

/** Minimal user projection for assignment pickers — no email/status/createdAt, unlike {@link AdminUserView}. */
public record UserBasicView(Long id, String name, Role role) {

    public static UserBasicView from(User user) {
        return new UserBasicView(user.getId(), user.getName(), user.getRole());
    }
}
