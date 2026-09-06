package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.ReportStatus;

/** One active member's report state for a given week. */
public record MemberSubmissionStatus(String memberName, Status status) {

    /**
     * Report progress, ordered least-to-most advanced so the least advanced wins when a member
     * has more than one report for the week. {@code NOT_STARTED} is the no-report-at-all case.
     */
    public enum Status {
        NOT_STARTED,
        DRAFT,
        NEEDS_CORRECTION,
        SUBMITTED,
        APPROVED;

        public static Status of(ReportStatus reportStatus) {
            return switch (reportStatus) {
                case DRAFT -> DRAFT;
                case NEEDS_CORRECTION -> NEEDS_CORRECTION;
                case SUBMITTED -> SUBMITTED;
                case APPROVED -> APPROVED;
            };
        }
    }
}
