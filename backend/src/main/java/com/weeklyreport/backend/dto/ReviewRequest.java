package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.domain.ReviewAction;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(@NotNull ReviewAction action, String comment) {

    @AssertTrue(message = "A comment is required when requesting changes")
    public boolean isCommentPresentWhenRequired() {
        return action != ReviewAction.CHANGES_REQUESTED || (comment != null && !comment.isBlank());
    }
}
