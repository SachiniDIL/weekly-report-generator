package com.weeklyreport.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * A report version's full content, replaced wholesale on every create/edit rather than
 * diffed item-by-item. Every field is optional so a report can be created with no content yet
 * and filled in gradually.
 */
public record ReportContentRequest(
        String tasksPlannedNext,
        String notes,
        String links,
        @Valid List<TaskEntryRequest> taskEntries,
        @Valid List<BlockerRequest> blockers,
        @Valid List<AchievementRequest> achievements,
        @Valid List<HoursBreakdownRequest> hoursBreakdown) {

    public static ReportContentRequest empty() {
        return new ReportContentRequest(null, null, null, List.of(), List.of(), List.of(), List.of());
    }

    public record TaskEntryRequest(
            @NotBlank String taskName,
            @NotBlank String priority,
            int plannedPct,
            int actualPct,
            @NotBlank String status,
            Integer timePlanned,
            Integer timeSpent,
            String deliverable) {}

    public record BlockerRequest(@NotBlank String description, boolean isKeyIssue) {}

    public record AchievementRequest(@NotBlank String description, boolean isKeyHighlight) {}

    public record HoursBreakdownRequest(@NotBlank String taskType, @NotNull BigDecimal hours) {}
}
