"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { describeError } from "@/lib/api-client";
import {
  createReport,
  submitReport,
  updateReportContent,
  type ReportContentRequest,
  type ReportResponse,
} from "@/lib/api/reports";
import { useToast } from "@/lib/toast/toast-context";

export interface ReportEditorValues {
  /** Only used when creating; ignored (and null) for an existing report. */
  projectId: number | null;
  weekStart: string;
  weekEnd: string;
  content: ReportContentRequest;
}

/** Create the report (new) or replace an existing one's current-version content. */
async function saveContent(
  reportId: number | null,
  values: ReportEditorValues,
): Promise<ReportResponse> {
  if (reportId != null) {
    return updateReportContent(reportId, values.content);
  }
  if (values.projectId == null) {
    throw new Error("A project must be selected to create a report");
  }
  return createReport({
    projectId: values.projectId,
    weekStart: values.weekStart,
    weekEnd: values.weekEnd,
    content: values.content,
  });
}

export function useSaveReportDraftMutation(reportId: number | null) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<ReportResponse, Error, ReportEditorValues>({
    mutationFn: (values) => saveContent(reportId, values),
    onSuccess: (report) => {
      queryClient.setQueryData(["report", report.id], report);
      toast.success("Draft saved.");
      if (reportId == null) {
        router.replace(`/reports/${report.id}`);
      }
    },
    onError: (error) => toast.error(describeError(error)),
  });
}

export function useSubmitReportMutation(reportId: number | null) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<ReportResponse, Error, ReportEditorValues>({
    mutationFn: async (values) => {
      const saved = await saveContent(reportId, values);
      return submitReport(saved.id);
    },
    onSuccess: (report) => {
      queryClient.setQueryData(["report", report.id], report);
      // Refresh the report lists and the sidebar's "changes requested" dot.
      queryClient.invalidateQueries({ queryKey: ["reports"] });
      toast.success("Report submitted for review.");
      router.replace(`/reports/${report.id}`);
    },
    onError: (error) => toast.error(describeError(error)),
  });
}
