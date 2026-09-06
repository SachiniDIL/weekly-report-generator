import { request } from "@/lib/api-client";

export interface DashboardSummary {
  totalSubmittedThisWeek: number;
  /** 0.0–1.0. */
  complianceRate: number;
  needsCorrectionCount: number;
  openBlockersCount: number;
}

export interface WeeklyTaskCompletionPoint {
  weekStart: string;
  weekEnd: string;
  completedTasks: number;
}

export type MemberWeekStatus =
  | "NOT_STARTED"
  | "DRAFT"
  | "NEEDS_CORRECTION"
  | "SUBMITTED"
  | "APPROVED";

export interface MemberSubmissionStatus {
  memberName: string;
  status: MemberWeekStatus;
}

export interface ProjectWorkloadPoint {
  projectName: string;
  taskCount: number;
}

export interface TaskTypeHoursPoint {
  taskType: string;
  totalHours: number;
}

export type DashboardSectionName = "blockers" | "achievements";

export interface SectionItem {
  description: string;
  /** The item the member flagged as the one that matters most this week. */
  key: boolean;
}

export interface MemberSectionView {
  memberName: string;
  status: MemberWeekStatus;
  items: SectionItem[];
}

export interface SectionComparisonParams {
  section: DashboardSectionName;
  weekStart?: string;
  weekEnd?: string;
}

export function getDashboardSummary(): Promise<DashboardSummary> {
  return request("/dashboard/summary", { method: "GET" });
}

export function getTasksCompletedTrend(weeks?: number): Promise<WeeklyTaskCompletionPoint[]> {
  return request("/dashboard/charts/tasks-completed-trend", { method: "GET", query: { weeks } });
}

export function getSubmissionStatusByMember(): Promise<MemberSubmissionStatus[]> {
  return request("/dashboard/charts/submission-status-by-member", { method: "GET" });
}

export function getWorkloadByProject(): Promise<ProjectWorkloadPoint[]> {
  return request("/dashboard/charts/workload-by-project", { method: "GET" });
}

export function getTimeByTaskType(): Promise<TaskTypeHoursPoint[]> {
  return request("/dashboard/charts/time-by-task-type", { method: "GET" });
}

export function getSectionComparison(
  params: SectionComparisonParams,
): Promise<MemberSectionView[]> {
  return request("/dashboard/section", {
    method: "GET",
    query: {
      section: params.section,
      weekStart: params.weekStart,
      weekEnd: params.weekEnd,
    },
  });
}
