"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import {
  reviewReport,
  type ReportResponse,
  type ReviewRequest,
} from "@/lib/api/reports";
import { useToast } from "@/lib/toast/toast-context";

/**
 * Approve or request changes on a report. On success we go to /projects — the manager landing
 * page — since there's no manager report list yet, so the outcome is confirmed with a toast.
 */
export function useReviewReportMutation(reportId: number) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<ReportResponse, Error, ReviewRequest>({
    mutationFn: (payload) => reviewReport(reportId, payload),
    onSuccess: (_report, request) => {
      queryClient.invalidateQueries({ queryKey: ["report", reportId] });
      toast.success(
        request.action === "APPROVED"
          ? "Report approved."
          : "Changes requested — the report was sent back to its author.",
      );
      router.push("/projects");
    },
  });
}
