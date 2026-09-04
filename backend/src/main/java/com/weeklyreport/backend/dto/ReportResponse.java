package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;

/** Full detail for a single report, including its current version's content. */
public record ReportResponse(
        Long id,
        Long projectId,
        String projectName,
        Long userId,
        String ownerName,
        LocalDate weekStart,
        LocalDate weekEnd,
        ReportStatus status,
        int currentVersionNo,
        ReportContentResponse content) {

    public static ReportResponse from(Report report, ReportContentResponse content) {
        return new ReportResponse(
                report.getId(),
                report.getProject().getId(),
                report.getProject().getName(),
                report.getUser().getId(),
                report.getUser().getName(),
                report.getWeekStart(),
                report.getWeekEnd(),
                report.getStatus(),
                report.getCurrentVersionNo(),
                content);
    }
}
