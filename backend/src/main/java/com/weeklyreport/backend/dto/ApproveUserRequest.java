package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Role;
import jakarta.validation.constraints.NotNull;

public record ApproveUserRequest(@NotNull Role role) {
}
