package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.dto.DashboardSummaryResponse;
import com.weeklyreport.backend.dto.MemberWeekStatus;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import java.util.List;
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
    private final DashboardCalendar calendar;
    private final TeamRosterForWeek teamRoster;

    public DashboardService(
            ReportRepository reportRepository,
            BlockerRepository blockerRepository,
            DashboardCalendar calendar,
            TeamRosterForWeek teamRoster) {
        this.reportRepository = reportRepository;
        this.blockerRepository = blockerRepository;
        this.calendar = calendar;
        this.teamRoster = teamRoster;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        DashboardCalendar.WeekRange week = calendar.resolveWeek(null, null);

        long totalSubmittedThisWeek =
                reportRepository.countByStatusNotAndWeekStartLessThanEqualAndWeekEndGreaterThanEqual(
                        ReportStatus.DRAFT, calendar.today(), calendar.today());
        long needsCorrectionCount = reportRepository.countByStatus(ReportStatus.NEEDS_CORRECTION);
        long openBlockersCount =
                blockerRepository.countOnCurrentVersionsOfReportsNotInStatus(ReportStatus.APPROVED);

        return new DashboardSummaryResponse(
                totalSubmittedThisWeek,
                weeklySubmissionCompliance(week),
                needsCorrectionCount,
                openBlockersCount);
    }

    /**
     * This week's submission compliance: the fraction of active members who have turned in a
     * report for the current week — one whose status is SUBMITTED, NEEDS_CORRECTION or APPROVED.
     * A member with only a DRAFT this week, or no report at all, counts as non-compliant. Reported
     * as 1.0 when there are no members.
     */
    private double weeklySubmissionCompliance(DashboardCalendar.WeekRange week) {
        List<TeamRosterForWeek.MemberWeek> roster = teamRoster.forWeek(week.start(), week.end());
        if (roster.isEmpty()) {
            return 1.0;
        }
        long submitted = roster.stream().filter(member -> hasSubmitted(member.status())).count();
        return (double) submitted / roster.size();
    }

    private static boolean hasSubmitted(MemberWeekStatus status) {
        return switch (status) {
            case NOT_STARTED, DRAFT -> false;
            case NEEDS_CORRECTION, SUBMITTED, APPROVED -> true;
        };
    }
}
