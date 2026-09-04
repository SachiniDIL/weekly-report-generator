package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;

public record ReportResponse(
        Long id,
        Long projectId,
        LocalDate weekStart,
        LocalDate weekEnd,
        ReportStatus status,
        int currentVersionNo,
        ReportContentResponse content) {

    public static ReportResponse from(Report report, ReportContentResponse content) {
        return new ReportResponse(
                report.getId(),
                report.getProjectId(),
                report.getWeekStart(),
                report.getWeekEnd(),
                report.getStatus(),
                report.getCurrentVersionNo(),
                content);
    }
}
