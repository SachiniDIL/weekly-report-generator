import { request } from "@/lib/api-client";

export type ReportStatus = "DRAFT" | "SUBMITTED" | "NEEDS_CORRECTION" | "APPROVED";
export type ReviewAction = "APPROVED" | "CHANGES_REQUESTED";

// --- content: request shapes (what the member sends on create/edit) ---

export interface TaskEntryRequest {
  taskName: string;
  priority: string;
  plannedPct: number;
  actualPct: number;
  status: string;
  timePlanned?: number | null;
  timeSpent?: number | null;
  deliverable?: string | null;
}

export interface BlockerRequest {
  description: string;
  isKeyIssue: boolean;
}

export interface AchievementRequest {
  description: string;
  isKeyHighlight: boolean;
}

export interface HoursBreakdownRequest {
  taskType: string;
  hours: number;
}

export interface ReportContentRequest {
  tasksPlannedNext?: string | null;
  notes?: string | null;
  links?: string | null;
  taskEntries?: TaskEntryRequest[];
  blockers?: BlockerRequest[];
  achievements?: AchievementRequest[];
  hoursBreakdown?: HoursBreakdownRequest[];
}

export interface CreateReportRequest {
  projectId: number;
  /** ISO calendar date, e.g. "2026-09-01". */
  weekStart: string;
  weekEnd: string;
  content?: ReportContentRequest;
}

// --- content: response shapes (what the backend returns, with row ids) ---

export interface TaskEntryView {
  id: number;
  taskName: string;
  priority: string;
  plannedPct: number;
  actualPct: number;
  status: string;
  timePlanned: number | null;
  timeSpent: number | null;
  deliverable: string | null;
}

export interface BlockerView {
  id: number;
  description: string;
  isKeyIssue: boolean;
}

export interface AchievementView {
  id: number;
  description: string;
  isKeyHighlight: boolean;
}

export interface HoursBreakdownView {
  id: number;
  taskType: string;
  hours: number;
}

export interface ReportContentResponse {
  reportVersionId: number;
  versionNo: number;
  /** ISO instant. */
  submittedAt: string;
  tasksPlannedNext: string | null;
  notes: string | null;
  links: string | null;
  taskEntries: TaskEntryView[];
  blockers: BlockerView[];
  achievements: AchievementView[];
  hoursBreakdown: HoursBreakdownView[];
}

export interface ReportResponse {
  id: number;
  projectId: number;
  projectName: string;
  userId: number;
  ownerName: string;
  weekStart: string;
  weekEnd: string;
  status: ReportStatus;
  currentVersionNo: number;
  content: ReportContentResponse;
}

export interface ReportListItemView {
  id: number;
  status: ReportStatus;
  weekStart: string;
  weekEnd: string;
  ownerName: string;
  projectName: string;
  currentVersionNo: number;
}

export interface ReviewCommentView {
  action: ReviewAction;
  comment: string | null;
  managerName: string;
  /** ISO instant. */
  createdAt: string;
}

export interface ReportVersionHistoryItem {
  content: ReportContentResponse;
  reviewComment: ReviewCommentView | null;
}

export interface ReviewRequest {
  action: ReviewAction;
  /** Required by the backend when action is CHANGES_REQUESTED. */
  comment?: string | null;
}

export interface ReportListFilters {
  projectId?: number;
  status?: ReportStatus;
  weekStart?: string;
  weekEnd?: string;
  /** Manager-only in practice; the backend forces a member's list to their own reports. */
  userId?: number;
}

export interface ListReportsParams extends ReportListFilters {
  page?: number;
  size?: number;
}

/** The slice of Spring's Page envelope the frontend actually reads. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export function createReport(payload: CreateReportRequest): Promise<ReportResponse> {
  return request("/reports", { method: "POST", body: payload });
}

export function updateReportContent(
  reportId: number,
  payload: ReportContentRequest,
): Promise<ReportResponse> {
  return request(`/reports/${reportId}`, { method: "PUT", body: payload });
}

export function submitReport(reportId: number): Promise<ReportResponse> {
  return request(`/reports/${reportId}/submit`, { method: "POST" });
}

/** Manager-only: approve or request changes on a submitted report. */
export function reviewReport(reportId: number, payload: ReviewRequest): Promise<ReportResponse> {
  return request(`/reports/${reportId}/review`, { method: "POST", body: payload });
}

export function getReportDetail(reportId: number): Promise<ReportResponse> {
  return request(`/reports/${reportId}`, { method: "GET" });
}

export function getReportVersionHistory(reportId: number): Promise<ReportVersionHistoryItem[]> {
  return request(`/reports/${reportId}/versions`, { method: "GET" });
}

export function listReports(params: ListReportsParams = {}): Promise<Page<ReportListItemView>> {
  return request("/reports", { method: "GET", query: { ...params } });
}
