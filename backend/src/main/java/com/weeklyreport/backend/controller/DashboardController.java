package com.weeklyreport.backend.controller;

import com.weeklyreport.backend.dto.DashboardSummaryResponse;
import com.weeklyreport.backend.dto.MemberSubmissionStatus;
import com.weeklyreport.backend.dto.ProjectWorkloadPoint;
import com.weeklyreport.backend.dto.TaskTypeHoursPoint;
import com.weeklyreport.backend.dto.WeeklyTaskCompletionPoint;
import com.weeklyreport.backend.service.DashboardChartService;
import com.weeklyreport.backend.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardChartService dashboardChartService;

    public DashboardController(
            DashboardService dashboardService, DashboardChartService dashboardChartService) {
        this.dashboardService = dashboardService;
        this.dashboardChartService = dashboardChartService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/charts/tasks-completed-trend")
    public List<WeeklyTaskCompletionPoint> tasksCompletedTrend(
            @RequestParam(defaultValue = "8") int weeks) {
        return dashboardChartService.tasksCompletedTrend(weeks);
    }

    @GetMapping("/charts/submission-status-by-member")
    public List<MemberSubmissionStatus> submissionStatusByMember(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd) {
        return dashboardChartService.submissionStatusByMember(weekStart, weekEnd);
    }

    @GetMapping("/charts/workload-by-project")
    public List<ProjectWorkloadPoint> workloadByProject(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd) {
        return dashboardChartService.workloadByProject(weekStart, weekEnd);
    }

    @GetMapping("/charts/time-by-task-type")
    public List<TaskTypeHoursPoint> timeByTaskType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd) {
        return dashboardChartService.timeByTaskType(weekStart, weekEnd);
    }
}
