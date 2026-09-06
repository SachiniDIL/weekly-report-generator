package com.weeklyreport.backend.dto;

/** One active member's report state for a given week — the "submission status by member" chart row. */
public record MemberSubmissionStatus(String memberName, MemberWeekStatus status) {}
