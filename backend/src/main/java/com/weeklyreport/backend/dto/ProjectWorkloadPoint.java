package com.weeklyreport.backend.dto;

/** Task-entry count for one project over a week — the dashboard's "workload by project" bar. */
public record ProjectWorkloadPoint(String projectName, Long taskCount) {}
