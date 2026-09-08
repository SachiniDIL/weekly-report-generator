import { useQuery } from "@tanstack/react-query";
import {
  listReports,
  type Page,
  type ReportListItemView,
} from "@/lib/api/reports";

/**
 * All of the member's own reports, newest week first — enough to tell which project/week
 * combinations they have already started, so the "new report" page can steer them away from
 * duplicating one.
 */
export function useMyReportCoverageQuery() {
  return useQuery<Page<ReportListItemView>>({
    queryKey: ["reports", "mine", "coverage"],
    queryFn: () =>
      listReports({ size: 100, sort: ["weekStart,desc", "id,desc"] }),
  });
}

/** Key for looking a report up by the project + week it covers. */
export function coverageKey(projectId: number, weekStart: string): string {
  return `${projectId}:${weekStart}`;
}
