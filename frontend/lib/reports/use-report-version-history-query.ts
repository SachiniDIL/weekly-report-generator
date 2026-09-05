import { useQuery } from "@tanstack/react-query";
import { getReportVersionHistory, type ReportVersionHistoryItem } from "@/lib/api/reports";

/**
 * Version history for one report, newest-first. Pass `enabled: false` when the caller doesn't
 * need it yet (e.g. the report isn't in NEEDS_CORRECTION).
 */
export function useReportVersionHistoryQuery(reportId: number, enabled = true) {
  return useQuery<ReportVersionHistoryItem[]>({
    queryKey: ["report", reportId, "versions"],
    queryFn: () => getReportVersionHistory(reportId),
    enabled: enabled && Number.isFinite(reportId),
    retry: false,
  });
}
