package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.ReportStatus;

/**
 * A member's report progress for one week, ordered least-to-most advanced (declaration order),
 * so the least advanced wins when a member has more than one report that week. {@code NOT_STARTED}
 * is the no-report-at-all case.
 */
public enum MemberWeekStatus {
    NOT_STARTED,
    DRAFT,
    NEEDS_CORRECTION,
    SUBMITTED,
    APPROVED;

    public static MemberWeekStatus of(ReportStatus reportStatus) {
        return switch (reportStatus) {
            case DRAFT -> DRAFT;
            case NEEDS_CORRECTION -> NEEDS_CORRECTION;
            case SUBMITTED -> SUBMITTED;
            case APPROVED -> APPROVED;
        };
    }

    public static MemberWeekStatus leastAdvanced(MemberWeekStatus left, MemberWeekStatus right) {
        return left.ordinal() <= right.ordinal() ? left : right;
    }
}
