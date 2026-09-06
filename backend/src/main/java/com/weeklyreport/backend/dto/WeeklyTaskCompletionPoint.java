package com.weeklyreport.backend.dto;

import java.time.LocalDate;

/** One point on the "tasks completed per week" trend — completion means {@code actualPct == 100}. */
public record WeeklyTaskCompletionPoint(
        LocalDate weekStart, LocalDate weekEnd, Long completedTasks) {}
