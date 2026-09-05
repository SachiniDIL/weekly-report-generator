package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.ReviewAction;
import com.weeklyreport.backend.domain.ReviewComment;
import java.time.Instant;

public record ReviewCommentView(ReviewAction action, String comment, String managerName, Instant createdAt) {

    public static ReviewCommentView from(ReviewComment reviewComment) {
        return new ReviewCommentView(
                reviewComment.getAction(),
                reviewComment.getComment(),
                reviewComment.getManager().getName(),
                reviewComment.getCreatedAt());
    }
}
