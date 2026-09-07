package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.ReportContentResponse;
import com.weeklyreport.backend.dto.ReportListFilters;
import com.weeklyreport.backend.dto.ReportResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Renders the reports matching a dashboard filter as a readable plain-text digest for an AI
 * prompt — one section per member, prose rather than JSON. Visibility and scoping are delegated
 * to {@link ReportQueryService}; this class only formats what it is given.
 */
@Service
public class ReportContextBuilder {

    static final String NO_DATA = "No reports match the selected filters.";

    private final ReportQueryService reportQueryService;

    public ReportContextBuilder(ReportQueryService reportQueryService) {
        this.reportQueryService = reportQueryService;
    }

    public String buildDigest(User manager, ReportListFilters filter) {
        List<ReportResponse> reports = reportQueryService.listReportsWithContent(manager, filter);
        if (reports.isEmpty()) {
            return NO_DATA;
        }

        StringBuilder digest = new StringBuilder();
        groupByMember(reports).forEach((memberName, memberReports) -> {
            digest.append("=== ").append(memberName).append(" ===\n");
            memberReports.forEach(report -> appendReport(digest, report));
            digest.append('\n');
        });
        return digest.toString().strip();
    }

    private static Map<String, List<ReportResponse>> groupByMember(List<ReportResponse> reports) {
        Map<String, List<ReportResponse>> byMember = new LinkedHashMap<>();
        for (ReportResponse report : reports) {
            byMember.computeIfAbsent(report.ownerName(), name -> new ArrayList<>()).add(report);
        }
        return byMember;
    }

    private static void appendReport(StringBuilder digest, ReportResponse report) {
        digest.append("Project ").append(report.projectName())
                .append(" — week ").append(report.weekStart())
                .append(" to ").append(report.weekEnd())
                .append(" (").append(report.status()).append(")\n");

        ReportContentResponse content = report.content();
        appendTasks(digest, content);
        appendBlockers(digest, content);
        appendAchievements(digest, content);
        appendHours(digest, content);
    }

    private static void appendTasks(StringBuilder digest, ReportContentResponse content) {
        if (content.taskEntries().isEmpty()) {
            return;
        }
        digest.append("  Tasks:\n");
        content.taskEntries().forEach(task -> digest.append("    - ")
                .append(task.taskName())
                .append(" [").append(task.priority()).append("] planned ")
                .append(task.plannedPct()).append("%, actual ")
                .append(task.actualPct()).append("%, status ")
                .append(task.status())
                .append(timeSpentSuffix(task.timeSpent()))
                .append('\n'));
    }

    private static void appendBlockers(StringBuilder digest, ReportContentResponse content) {
        if (content.blockers().isEmpty()) {
            return;
        }
        digest.append("  Blockers:\n");
        content.blockers().forEach(blocker -> digest.append("    - ")
                .append(blocker.description())
                .append(blocker.isKeyIssue() ? " (KEY ISSUE)" : "")
                .append('\n'));
    }

    private static void appendAchievements(StringBuilder digest, ReportContentResponse content) {
        if (content.achievements().isEmpty()) {
            return;
        }
        digest.append("  Achievements:\n");
        content.achievements().forEach(achievement -> digest.append("    - ")
                .append(achievement.description())
                .append(achievement.isKeyHighlight() ? " (KEY HIGHLIGHT)" : "")
                .append('\n'));
    }

    private static void appendHours(StringBuilder digest, ReportContentResponse content) {
        if (content.hoursBreakdown().isEmpty()) {
            return;
        }
        digest.append("  Hours:\n");
        content.hoursBreakdown().forEach(entry -> digest.append("    - ")
                .append(entry.taskType()).append(": ").append(entry.hours()).append("h\n"));
    }

    private static String timeSpentSuffix(Integer timeSpent) {
        return timeSpent == null ? "" : ", time spent " + timeSpent + "h";
    }
}
