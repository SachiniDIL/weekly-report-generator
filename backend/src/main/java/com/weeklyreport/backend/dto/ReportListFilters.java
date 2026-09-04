package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;

/**
 * Optional filters for GET /reports. {@code userId} is manager-only in practice — for a MEMBER
 * caller, {@link com.weeklyreport.backend.service.ReportQueryService} overrides it to their own
 * id rather than honoring whatever was requested.
 */
public record ReportListFilters(
        Long projectId, ReportStatus status, LocalDate weekStart, LocalDate weekEnd, Long userId) {}
