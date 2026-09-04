package com.weeklyreport.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * A report's identity — project and week — fixed at creation and never editable afterward, plus
 * the optional initial content for its first version.
 */
public record CreateReportRequest(
        @NotNull Long projectId,
        @NotNull LocalDate weekStart,
        @NotNull LocalDate weekEnd,
        @Valid ReportContentRequest content) {}
