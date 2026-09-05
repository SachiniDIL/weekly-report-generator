"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { reviewReport, type ReportResponse, type ReviewRequest } from "@/lib/api/reports";

/**
 * Approve or request changes on a report. On success we go to /projects — the manager landing
 * page — since there's no manager report list yet.
 */
export function useReviewReportMutation(reportId: number) {
  const router = useRouter();
  const queryClient = useQueryClient();

  return useMutation<ReportResponse, Error, ReviewRequest>({
    mutationFn: (payload) => reviewReport(reportId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["report", reportId] });
      router.push("/projects");
    },
  });
}
