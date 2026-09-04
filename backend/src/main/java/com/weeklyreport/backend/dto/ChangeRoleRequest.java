package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {
}
