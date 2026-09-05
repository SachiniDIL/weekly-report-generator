import type { ReviewAction } from "@/lib/api/reports";

export interface ReviewInput {
  action: ReviewAction;
  comment: string;
}

/**
 * Mirrors the backend's rule: a comment is mandatory when requesting changes, optional when
 * approving. Returns the message to show, or null when the input is fine to send.
 */
export function validateReviewInput({ action, comment }: ReviewInput): string | null {
  if (action === "CHANGES_REQUESTED" && comment.trim() === "") {
    return "Explain what needs to change before requesting changes.";
  }
  return null;
}
