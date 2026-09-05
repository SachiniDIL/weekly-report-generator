import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { listReports, type Page, type ReportListItemView } from "@/lib/api/reports";

/** A generous default: a member's own history is much smaller than a cross-team view. */
export const MY_REPORTS_PAGE_SIZE = 25;

// The backend's default sort is id-ascending, so ask for most-recent-week-first explicitly;
// id is the tie-breaker that keeps pagination stable when several reports share a week.
const SORT = ["weekStart,desc", "id,desc"];

export function useMyReportsQuery(page: number) {
  return useQuery<Page<ReportListItemView>>({
    queryKey: ["reports", "mine", page],
    queryFn: () => listReports({ page, size: MY_REPORTS_PAGE_SIZE, sort: SORT }),
    placeholderData: keepPreviousData,
  });
}
