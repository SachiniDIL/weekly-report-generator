package com.weeklyreport.backend.dto;

import java.math.BigDecimal;

/** Total hours logged against one task type over a week, team-wide. */
public record TaskTypeHoursPoint(String taskType, BigDecimal totalHours) {}
