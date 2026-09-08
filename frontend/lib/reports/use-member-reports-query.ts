import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
  listReports,
  type Page,
  type ReportListItemView,
} from "@/lib/api/reports";
import {
  REPORT_HISTORY_PAGE_SIZE,
  REPORT_HISTORY_SORT,
} from "@/lib/reports/report-history-query-config";

/** A manager's view of one member's full report history — the same list endpoint, scoped by userId. */
export function useMemberReportsQuery(userId: number, page: number) {
  return useQuery<Page<ReportListItemView>>({
    queryKey: ["reports", "by-member", userId, page],
    queryFn: () =>
      listReports({
        userId,
        page,
        size: REPORT_HISTORY_PAGE_SIZE,
        sort: REPORT_HISTORY_SORT,
      }),
    enabled: Number.isFinite(userId),
    placeholderData: keepPreviousData,
  });
}
