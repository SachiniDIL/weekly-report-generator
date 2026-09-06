package com.weeklyreport.backend.dto;

import java.util.List;

/**
 * One team member's blockers or achievements for a week. {@code items} is empty when the member
 * has no report for the week ({@code status == NOT_STARTED}) or it's still a DRAFT — the manager
 * still sees the member, with the status explaining the empty section.
 */
public record MemberSectionView(String memberName, MemberWeekStatus status, List<SectionItem> items) {

    /** {@code key} is the flagged "key issue" / "key highlight" item. */
    public record SectionItem(String description, boolean key) {}
}
