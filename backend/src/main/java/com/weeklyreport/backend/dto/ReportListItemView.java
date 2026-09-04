package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;

/** A lighter row for GET /reports — no version content, just what a list view needs. */
public record ReportListItemView(
        Long id,
        ReportStatus status,
        LocalDate weekStart,
        LocalDate weekEnd,
        String ownerName,
        String projectName,
        int currentVersionNo) {

    public static ReportListItemView from(Report report) {
        return new ReportListItemView(
                report.getId(),
                report.getStatus(),
                report.getWeekStart(),
                report.getWeekEnd(),
                report.getUser().getName(),
                report.getProject().getName(),
                report.getCurrentVersionNo());
    }
}
