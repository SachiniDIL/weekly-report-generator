package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.MemberSubmissionStatus;
import com.weeklyreport.backend.dto.ProjectWorkloadPoint;
import com.weeklyreport.backend.dto.TaskTypeHoursPoint;
import com.weeklyreport.backend.dto.WeeklyTaskCompletionPoint;
import com.weeklyreport.backend.repository.DashboardChartRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.UserRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregation queries behind the manager dashboard charts. */
@Service
public class DashboardChartService {

    private static final int MAX_TREND_WEEKS = 52;
    private static final Sort BY_NAME = Sort.by("name").ascending();

    private final DashboardChartRepository chartRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public DashboardChartService(
            DashboardChartRepository chartRepository,
            ReportRepository reportRepository,
            UserRepository userRepository,
            Clock clock) {
        this.chartRepository = chartRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<WeeklyTaskCompletionPoint> tasksCompletedTrend(int weeks) {
        // Clamp so a bogus ?weeks= can't ask for an unbounded scan.
        int limit = Math.clamp(weeks, 1, MAX_TREND_WEEKS);
        List<WeeklyTaskCompletionPoint> mostRecent = new ArrayList<>(
                chartRepository.tasksCompletedByWeekNewestFirst(today()).stream().limit(limit).toList());
        Collections.reverse(mostRecent);
        return mostRecent;
    }

    @Transactional(readOnly = true)
    public List<MemberSubmissionStatus> submissionStatusByMember(
            LocalDate weekStart, LocalDate weekEnd) {
        Week week = resolveWeek(weekStart, weekEnd);

        Map<Long, MemberSubmissionStatus.Status> statusByUserId =
                reportRepository.findByWeekStartAndWeekEnd(week.start(), week.end()).stream()
                        .collect(Collectors.toMap(
                                report -> report.getUser().getId(),
                                report -> MemberSubmissionStatus.Status.of(report.getStatus()),
                                DashboardChartService::leastAdvanced));

        return userRepository.findByStatusAndRole(UserStatus.ACTIVE, Role.MEMBER, BY_NAME).stream()
                .map(member -> new MemberSubmissionStatus(
                        member.getName(),
                        statusByUserId.getOrDefault(
                                member.getId(), MemberSubmissionStatus.Status.NOT_STARTED)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectWorkloadPoint> workloadByProject(LocalDate weekStart, LocalDate weekEnd) {
        Week week = resolveWeek(weekStart, weekEnd);
        return chartRepository.taskCountByProject(week.start(), week.end()).stream()
                .sorted(Comparator.comparingLong(ProjectWorkloadPoint::taskCount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskTypeHoursPoint> timeByTaskType(LocalDate weekStart, LocalDate weekEnd) {
        Week week = resolveWeek(weekStart, weekEnd);
        return chartRepository.hoursByTaskType(week.start(), week.end()).stream()
                .sorted(Comparator.comparing(TaskTypeHoursPoint::totalHours).reversed())
                .toList();
    }

    private static MemberSubmissionStatus.Status leastAdvanced(
            MemberSubmissionStatus.Status left, MemberSubmissionStatus.Status right) {
        return left.ordinal() <= right.ordinal() ? left : right;
    }

    private LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    /** The ISO week (Mon–Sun) containing today, unless the caller pinned both endpoints. */
    private Week resolveWeek(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart != null && weekEnd != null) {
            return new Week(weekStart, weekEnd);
        }
        LocalDate monday = today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new Week(monday, monday.plusDays(6));
    }

    private record Week(LocalDate start, LocalDate end) {}
}
