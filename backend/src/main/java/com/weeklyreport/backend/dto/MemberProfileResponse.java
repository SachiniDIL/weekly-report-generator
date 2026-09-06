package com.weeklyreport.backend.dto;

import java.util.List;

/**
 * A manager's view of one team member: identity plus all-time stats. The member's report list
 * is not included — {@code GET /reports?userId=} already serves it.
 */
public record MemberProfileResponse(
        Long id,
        String name,
        String email,
        long totalReportsSubmitted,
        long needsCorrectionCount,
        List<TaskTypeHoursPoint> hoursByTaskType) {}
