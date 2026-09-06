import { useQuery } from "@tanstack/react-query";
import {
  getDashboardSummary,
  getSubmissionStatusByMember,
  getTasksCompletedTrend,
  getTimeByTaskType,
  getWorkloadByProject,
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
