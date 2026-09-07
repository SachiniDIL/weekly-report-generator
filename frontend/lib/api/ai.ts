import { request } from "@/lib/api-client";

/** Both AI endpoints return the same shape: a single plain-text message. */
export interface AiMessageResponse {
  message: string;
}

/** The dashboard scope the AI answers within. Every field is optional; omit for "all reports". */
export interface AiScopeFilters {
  weekStart?: string;
  weekEnd?: string;
  projectId?: number;
  userId?: number;
}

export interface ChatRequest extends AiScopeFilters {
  question: string;
}

export function sendChatMessage(payload: ChatRequest): Promise<AiMessageResponse> {
  return request("/ai/chat", { method: "POST", body: payload });
}

/** The summary has no member scope — it is always team-wide. */
export type SummaryFilters = Omit<AiScopeFilters, "userId">;

export function generateSummary(filters: SummaryFilters = {}): Promise<AiMessageResponse> {
  return request("/ai/summary", { method: "GET", query: { ...filters } });
}
