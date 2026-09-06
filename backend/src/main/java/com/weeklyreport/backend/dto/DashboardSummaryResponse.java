package com.weeklyreport.backend.dto;

/**
 * Headline numbers for the manager dashboard.
 *
 * @param totalSubmittedThisWeek reports whose week contains today and that are no longer a DRAFT
 * @param complianceRate submitted / (submitted + late), 0.0–1.0; 1.0 when nothing is due yet
 * @param needsCorrectionCount reports currently in NEEDS_CORRECTION
 * @param openBlockersCount blockers on the current version of every not-yet-APPROVED report
 */
public record DashboardSummaryResponse(
        long totalSubmittedThisWeek,
        double complianceRate,
        long needsCorrectionCount,
        long openBlockersCount) {}
