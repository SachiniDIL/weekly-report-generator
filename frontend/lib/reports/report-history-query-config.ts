/** Shared paging/sort for any "one person's report history" list (a member's own, or a manager's view of one member). */
export const REPORT_HISTORY_PAGE_SIZE = 25;

// The backend's default sort is id-ascending, so ask for most-recent-week-first explicitly;
// id is the tie-breaker that keeps pagination stable when several reports share a week.
export const REPORT_HISTORY_SORT = ["weekStart,desc", "id,desc"];
