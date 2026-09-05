import { useQuery } from "@tanstack/react-query";
import { getReportDetail, type ReportResponse } from "@/lib/api/reports";

/**
 * Fetches one report's detail. Retries are disabled so a 403/404 (the backend deciding this
 * caller can't see this specific report) surfaces immediately instead of after backoff.
 */
export function useReportDetailQuery(reportId: number) {
  return useQuery<ReportResponse>({
    queryKey: ["report", reportId],
    queryFn: () => getReportDetail(reportId),
    enabled: Number.isFinite(reportId),
    retry: false,
  });
}
