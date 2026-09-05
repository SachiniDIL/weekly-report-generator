"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import {
  createReport,
  submitReport,
  updateReportContent,
  type ReportContentRequest,
  type ReportResponse,
} from "@/lib/api/reports";

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

  return useMutation<ReportResponse, Error, ReportEditorValues>({
    mutationFn: (values) => saveContent(reportId, values),
    onSuccess: (report) => {
      queryClient.setQueryData(["report", report.id], report);
      if (reportId == null) {
        router.replace(`/reports/${report.id}`);
      }
    },
  });
}

export function useSubmitReportMutation(reportId: number | null) {
  const router = useRouter();
  const queryClient = useQueryClient();

  return useMutation<ReportResponse, Error, ReportEditorValues>({
    mutationFn: async (values) => {
      const saved = await saveContent(reportId, values);
      return submitReport(saved.id);
    },
    onSuccess: (report) => {
      queryClient.setQueryData(["report", report.id], report);
      router.replace(`/reports/${report.id}`);
    },
  });
}
