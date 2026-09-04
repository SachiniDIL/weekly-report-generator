package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Admin direct-create payload. Unlike {@link RegisterRequest} this accepts a role, because
 * only an admin can reach the endpoint that consumes it.
 */
public record AdminCreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
        @NotNull Role role) {
}
