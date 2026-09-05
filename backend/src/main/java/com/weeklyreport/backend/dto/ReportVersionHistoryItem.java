package com.weeklyreport.backend.dto;

/** One row of GET /reports/{id}/versions. {@code reviewComment} is null for a version no manager has reviewed yet. */
public record ReportVersionHistoryItem(ReportContentResponse content, ReviewCommentView reviewComment) {}
