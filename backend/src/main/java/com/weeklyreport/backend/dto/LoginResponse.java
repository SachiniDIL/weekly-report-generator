package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Role;

/**
 * Returned on successful login: the bearer token plus just enough of the user for the
 * frontend to render its shell without an immediate follow-up request.
 */
public record LoginResponse(String token, UserSummary user) {

    public record UserSummary(Long id, String name, Role role) {
    }
}
