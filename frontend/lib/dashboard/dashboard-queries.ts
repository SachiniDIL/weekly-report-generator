import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
  getDashboardSummary,
  getMemberProfile,
  getSectionComparison,
  getSubmissionStatusByMember,
  getTasksCompletedTrend,
  getTimeByTaskType,
  getWorkloadByProject,
  type SectionComparisonParams,
} from "@/lib/api/dashboard";

export function useDashboardSummaryQuery() {
  return useQuery({ queryKey: ["dashboard", "summary"], queryFn: getDashboardSummary });
}

export function useTasksCompletedTrendQuery() {
  return useQuery({
    queryKey: ["dashboard", "tasks-completed-trend"],
    queryFn: () => getTasksCompletedTrend(),
  });
}

export function useSubmissionStatusQuery() {
  return useQuery({
    queryKey: ["dashboard", "submission-status"],
    queryFn: getSubmissionStatusByMember,
  });
}

export function useWorkloadByProjectQuery() {
  return useQuery({
    queryKey: ["dashboard", "workload-by-project"],
    queryFn: getWorkloadByProject,
  });
}

export function useTimeByTaskTypeQuery() {
  return useQuery({ queryKey: ["dashboard", "time-by-task-type"], queryFn: getTimeByTaskType });
}

export function useMemberProfileQuery(userId: number) {
  return useQuery({
    queryKey: ["dashboard", "team", userId],
    queryFn: () => getMemberProfile(userId),
    enabled: Number.isFinite(userId),
    retry: false,
  });
}

export function useSectionComparisonQuery(params: SectionComparisonParams) {
  return useQuery({
    queryKey: [
      "dashboard",
      "section",
      params.section,
      params.weekStart ?? null,
      params.weekEnd ?? null,
    ],
    queryFn: () => getSectionComparison(params),
    placeholderData: keepPreviousData,
  });
}
