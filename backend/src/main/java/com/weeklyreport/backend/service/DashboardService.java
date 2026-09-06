package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.dto.DashboardSummaryResponse;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregate metrics for the manager dashboard — cross-cutting over every report, so kept apart
 * from {@link ReportQueryService}, which is per-report and visibility-scoped.
 */
@Service
public class DashboardService {

    private final ReportRepository reportRepository;
    private final BlockerRepository blockerRepository;
    private final Clock clock;

    public DashboardService(
            ReportRepository reportRepository, BlockerRepository blockerRepository, Clock clock) {
        this.reportRepository = reportRepository;
        this.blockerRepository = blockerRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);

        long totalSubmittedThisWeek =
                reportRepository.countByStatusNotAndWeekStartLessThanEqualAndWeekEndGreaterThanEqual(
                        ReportStatus.DRAFT, today, today);
        long needsCorrectionCount = reportRepository.countByStatus(ReportStatus.NEEDS_CORRECTION);
        long openBlockersCount =
                blockerRepository.countOnCurrentVersionsOfReportsNotInStatus(ReportStatus.APPROVED);

        return new DashboardSummaryResponse(
                totalSubmittedThisWeek, complianceRate(today), needsCorrectionCount, openBlockersCount);
    }

    /**
     * Submitted work against missed deadlines. A DRAFT whose week hasn't ended yet counts
     * neither way — no deadline has been missed. With nothing submitted or overdue, compliance
     * is reported as full.
     */
    private double complianceRate(LocalDate today) {
        long submitted =
                reportRepository.countByStatusNotAndWeekStartLessThanEqual(ReportStatus.DRAFT, today);
        long late = reportRepository.countByStatusAndWeekEndLessThan(ReportStatus.DRAFT, today);
        long total = submitted + late;
        return total == 0 ? 1.0 : (double) submitted / total;
    }
}
