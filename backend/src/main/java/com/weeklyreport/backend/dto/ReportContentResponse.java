package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Achievement;
import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.HoursBreakdown;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.domain.TaskEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * The current version's content as shown back to the client — a separate shape from
 * {@link ReportContentRequest} because the response needs each child row's id (so a later edit
 * or per-item reference can target a specific row) and version metadata the request has no use
 * for.
 */
public record ReportContentResponse(
        Long reportVersionId,
        int versionNo,
        Instant submittedAt,
        String tasksPlannedNext,
        String notes,
        String links,
        List<TaskEntryView> taskEntries,
        List<BlockerView> blockers,
        List<AchievementView> achievements,
        List<HoursBreakdownView> hoursBreakdown) {

    public static ReportContentResponse from(
            ReportVersion version,
            List<TaskEntry> taskEntries,
            List<Blocker> blockers,
            List<Achievement> achievements,
            List<HoursBreakdown> hoursBreakdowns) {
        return new ReportContentResponse(
                version.getId(),
                version.getVersionNo(),
                version.getSubmittedAt(),
                version.getTasksPlannedNext(),
                version.getNotes(),
                version.getLinks(),
                taskEntries.stream().map(TaskEntryView::from).toList(),
                blockers.stream().map(BlockerView::from).toList(),
                achievements.stream().map(AchievementView::from).toList(),
                hoursBreakdowns.stream().map(HoursBreakdownView::from).toList());
    }

    public record TaskEntryView(
            Long id,
            String taskName,
            String priority,
            int plannedPct,
            int actualPct,
            String status,
            Integer timePlanned,
            Integer timeSpent,
            String deliverable) {

        static TaskEntryView from(TaskEntry entry) {
            return new TaskEntryView(
                    entry.getId(),
                    entry.getTaskName(),
                    entry.getPriority(),
                    entry.getPlannedPct(),
                    entry.getActualPct(),
                    entry.getStatus(),
                    entry.getTimePlanned(),
                    entry.getTimeSpent(),
                    entry.getDeliverable());
        }
    }

    public record BlockerView(Long id, String description, boolean isKeyIssue) {

        static BlockerView from(Blocker blocker) {
            return new BlockerView(blocker.getId(), blocker.getDescription(), blocker.isKeyIssue());
        }
    }

    public record AchievementView(Long id, String description, boolean isKeyHighlight) {

        static AchievementView from(Achievement achievement) {
            return new AchievementView(
                    achievement.getId(), achievement.getDescription(), achievement.isKeyHighlight());
        }
    }

    public record HoursBreakdownView(Long id, String taskType, BigDecimal hours) {

        static HoursBreakdownView from(HoursBreakdown hoursBreakdown) {
            return new HoursBreakdownView(
                    hoursBreakdown.getId(), hoursBreakdown.getTaskType(), hoursBreakdown.getHours());
        }
    }
}
