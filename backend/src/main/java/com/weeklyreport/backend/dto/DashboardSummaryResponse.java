package com.weeklyreport.backend.dto;

/**
 * Headline numbers for the manager dashboard.
 *
 * @param totalSubmittedThisWeek reports whose week contains today and that are no longer a DRAFT
 * @param complianceRate fraction of active members (0.0–1.0) who have turned in a report for the
 *     current week — SUBMITTED, NEEDS_CORRECTION or APPROVED; 1.0 when there are no members
 * @param needsCorrectionCount reports currently in NEEDS_CORRECTION
 * @param openBlockersCount blockers on the current version of every not-yet-APPROVED report
 */
public record DashboardSummaryResponse(
        long totalSubmittedThisWeek,
        double complianceRate,
        long needsCorrectionCount,
        long openBlockersCount) {}
