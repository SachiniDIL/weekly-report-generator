import { ApiError } from "@/lib/api-client";

/** True when the backend refused this specific report — the member sees a soft "not accessible" note, not an error dump. */
export function isReportInaccessible(error: unknown): boolean {
  return error instanceof ApiError && (error.status === 403 || error.status === 404);
}
