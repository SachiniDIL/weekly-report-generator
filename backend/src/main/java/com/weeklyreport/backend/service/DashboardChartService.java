package com.weeklyreport.backend.service;

import com.weeklyreport.backend.dto.MemberSubmissionStatus;
import com.weeklyreport.backend.dto.ProjectWorkloadPoint;
import com.weeklyreport.backend.dto.TaskTypeHoursPoint;
import com.weeklyreport.backend.dto.WeeklyTaskCompletionPoint;
import com.weeklyreport.backend.repository.DashboardChartRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregation queries behind the manager dashboard charts. */
@Service
public class DashboardChartService {

    private static final int MAX_TREND_WEEKS = 52;

    private final DashboardChartRepository chartRepository;
    private final DashboardCalendar calendar;
    private final TeamRosterForWeek teamRoster;

    public DashboardChartService(
            DashboardChartRepository chartRepository,
            DashboardCalendar calendar,
            TeamRosterForWeek teamRoster) {
        this.chartRepository = chartRepository;
        this.calendar = calendar;
        this.teamRoster = teamRoster;
    }

    @Transactional(readOnly = true)
    public List<WeeklyTaskCompletionPoint> tasksCompletedTrend(int weeks) {
        // Clamp so a bogus ?weeks= can't ask for an unbounded scan.
        int limit = Math.clamp(weeks, 1, MAX_TREND_WEEKS);
        List<WeeklyTaskCompletionPoint> mostRecent = new ArrayList<>(
                chartRepository.tasksCompletedByWeekNewestFirst(calendar.today()).stream()
                        .limit(limit)
                        .toList());
        Collections.reverse(mostRecent);
        return mostRecent;
    }

    @Transactional(readOnly = true)
    public List<MemberSubmissionStatus> submissionStatusByMember(LocalDate weekStart, LocalDate weekEnd) {
        DashboardCalendar.WeekRange week = calendar.resolveWeek(weekStart, weekEnd);
        return teamRoster.forWeek(week.start(), week.end()).stream()
                .map(member -> new MemberSubmissionStatus(member.name(), member.status()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectWorkloadPoint> workloadByProject(LocalDate weekStart, LocalDate weekEnd) {
        DashboardCalendar.WeekRange week = calendar.resolveWeek(weekStart, weekEnd);
        return chartRepository.taskCountByProject(week.start(), week.end());
    }

    @Transactional(readOnly = true)
    public List<TaskTypeHoursPoint> timeByTaskType(LocalDate weekStart, LocalDate weekEnd) {
        DashboardCalendar.WeekRange week = calendar.resolveWeek(weekStart, weekEnd);
        return chartRepository.hoursByTaskType(null, week.start(), week.end());
    }
}
