package com.weeklyreport.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Body of {@code POST /ai/chat}: a free-text question plus the dashboard's currently active
 * scope filter. Every filter field is optional and applies only when present.
 */
public record AiChatRequest(
        @NotBlank String question,
        LocalDate weekStart,
        LocalDate weekEnd,
        Long projectId,
        Long userId) {}
