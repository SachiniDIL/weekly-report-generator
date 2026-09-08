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

export function useMyReportsQuery(page: number) {
  return useQuery<Page<ReportListItemView>>({
    queryKey: ["reports", "mine", page],
    queryFn: () =>
      listReports({
        page,
        size: REPORT_HISTORY_PAGE_SIZE,
        sort: REPORT_HISTORY_SORT,
      }),
    placeholderData: keepPreviousData,
  });
}
