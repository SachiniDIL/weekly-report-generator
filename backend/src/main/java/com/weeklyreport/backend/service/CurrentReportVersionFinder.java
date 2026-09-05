package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportVersion;
import com.weeklyreport.backend.repository.ReportVersionRepository;
import org.springframework.stereotype.Component;

/**
 * Looks up a report's current version — shared by {@link ReportService}, {@link
 * ReportQueryService} and {@link ReportReviewService} so this exact lookup (and what it means if
 * it's ever missing) lives in one place rather than three.
 */
@Component
class CurrentReportVersionFinder {

    private final ReportVersionRepository reportVersionRepository;

    CurrentReportVersionFinder(ReportVersionRepository reportVersionRepository) {
        this.reportVersionRepository = reportVersionRepository;
    }

    ReportVersion get(Report report) {
        return reportVersionRepository
                .findByReportIdAndVersionNo(report.getId(), report.getCurrentVersionNo())
                .orElseThrow(() -> new IllegalStateException(
                        "Report " + report.getId() + " has no version " + report.getCurrentVersionNo()));
    }
}
