import { useQuery } from "@tanstack/react-query";
import {
  listReports,
  type Page,
  type ReportListItemView,
} from "@/lib/api/reports";

/** Submitted reports awaiting a manager's review, oldest-week first. */
export function useReviewQueueQuery(options: { enabled?: boolean } = {}) {
  return useQuery<Page<ReportListItemView>>({
    queryKey: ["reports", "review-queue"],
    queryFn: () =>
      listReports({
        status: "SUBMITTED",
        size: 100,
        sort: ["weekStart,asc", "id,asc"],
      }),
    enabled: options.enabled ?? true,
  });
}
